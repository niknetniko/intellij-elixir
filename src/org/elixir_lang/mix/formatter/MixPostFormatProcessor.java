package org.elixir_lang.mix.formatter;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.ExternalFormatProcessor;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import org.elixir_lang.psi.ElixirFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Niko Strijbol
 */
@SuppressWarnings({"MissingRecentApi", "UnstableApiUsage"})
public class MixPostFormatProcessor implements PostFormatProcessor {

    @NotNull
    @Override
    public PsiElement processElement(@NotNull PsiElement source, @NotNull CodeStyleSettings settings) {
        return source;
    }

    @NotNull
    @Override
    public TextRange processText(@NotNull PsiFile source, @NotNull TextRange rangeToReformat, @NotNull CodeStyleSettings settings) {
        if (!(source instanceof ElixirFile)) return rangeToReformat;
        if (!rangeToReformat.equalsToRange(0, source.getTextLength())) return rangeToReformat;
        TextRange range = ExternalFormatProcessor.formatRangeInFile(source, rangeToReformat, false, false);
        return range != null ? range : rangeToReformat;
    }
}
