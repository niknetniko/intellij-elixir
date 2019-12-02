package org.elixir_lang.eex.format;

import com.intellij.lang.Language;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.FileIndentOptionsProvider;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.elixir_lang.eex.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


// See https://github.com/JetBrains/intellij-plugins/blob/master/handlebars/src/com/dmarcotte/handlebars/format/HbFileIndentOptionsProvider.java
public class IndentOptionsProvider extends FileIndentOptionsProvider {
    @Nullable
    @Override
    public CommonCodeStyleSettings.IndentOptions getIndentOptions(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
        if (file instanceof File) {
            FileViewProvider provider = file.getViewProvider();
            if (provider instanceof TemplateLanguageFileViewProvider) {
                Language language = ((TemplateLanguageFileViewProvider)provider).getTemplateDataLanguage();
                return settings.getCommonSettings(language).getIndentOptions();
            }
        }
        return null;
    }
}
