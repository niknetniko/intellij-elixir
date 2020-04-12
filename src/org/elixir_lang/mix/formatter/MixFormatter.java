package org.elixir_lang.mix.formatter;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.ExternalFormatProcessor;
import org.elixir_lang.ElixirFileType;
import org.elixir_lang.eex.file.Type;
import org.elixir_lang.mix.Configuration;
import org.elixir_lang.mix.configuration.Factory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Use {@code mix format} to format Elixir code.
 *
 * @author Niko Strijbol
 */
@SuppressWarnings({"UnstableApiUsage", "MissingRecentApi"})
public class MixFormatter implements ExternalFormatProcessor {

    private static final Logger LOG = Logger.getInstance(MixFormatter.class);

    @Override
    public boolean activeForFile(@NotNull PsiFile source) {
        return source.getFileType() instanceof ElixirFileType || source.getFileType() instanceof ElixirFileType.ElixirScriptFileType;
    }

    @Nullable
    @Override
    public TextRange format(@NotNull PsiFile source, @NotNull TextRange range, boolean canChangeWhiteSpacesOnly, boolean keepLineBreaks) {
        LOG.warn("Doing formatting!");
        doFormat(source.getProject(), source.getVirtualFile());
        return range;
    }

    @Nullable
    @Override
    public String indent(@NotNull PsiFile source, int lineStartOffset) {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return "mix format";
    }

    private void doFormat(@NotNull Project project, @Nullable VirtualFile file) {
        if (file == null || !file.exists()) return;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile == null || !this.activeForFile(psiFile)) return;

        String filePath = file.getPath();
        String realPath = FileUtil.toSystemDependentName(filePath);
        if (!new File(realPath).exists()) return;

        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return;

        documentManager.saveDocument(document);

        try {
            Factory factory = Factory.INSTANCE;
            Configuration configuration = new Configuration("formatter", project, factory);
            configuration.setProgramParameters("format");

            GeneralCommandLine commandLine = configuration.commandLine()
                    .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                    .withParameters(realPath);

            OSProcessHandler handler = new OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8));
            handler.addProcessListener(new CapturingProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    int exitCode = event.getExitCode();
                    if (exitCode == 0) {
                        psiFile.getVirtualFile().refresh(false, false);
                    } else {
                        showFailedNotification(project, getOutput().getStderr());
                    }
                }
            });
            ApplicationManager.getApplication().executeOnPooledThread(handler::startNotify);
        } catch (ExecutionException e) {
            showFailedNotification(project, e.getMessage());
        }
    }

    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Mix formatter errors", NotificationDisplayType.BALLOON, true);

    private static void showFailedNotification(Project project, String stderr) {
        Notification notification = NOTIFICATION_GROUP.createNotification(stderr, NotificationType.ERROR);
        notification.notify(project);
    }
}
