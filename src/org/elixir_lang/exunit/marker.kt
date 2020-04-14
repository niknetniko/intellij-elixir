package org.elixir_lang.exunit

import com.intellij.execution.TestStateStorage
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.refactoring.suggested.startOffset
import org.codehaus.plexus.interpolation.os.Os
import org.elixir_lang.psi.ElixirTypes

class ExUnitLineMarkerProvider: RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {

        val type = PsiUtilCore.getElementType(element)
        if (type != ElixirTypes.IDENTIFIER_TOKEN) {
            return null
        }

        if (!isUnderTestSources(element)) {
            return null
        }

        val isClass = when (element.text) {
            "test" -> false
            "describe" -> true
            "defmodule" -> true
            else -> null
        } ?: return null

        val m = PsiDocumentManager.getInstance(element.project)
        val doc = m.getDocument(element.containingFile) ?: return null
        val number = doc.getLineNumber(element.startOffset)
        var url = "file://${element.containingFile.virtualFile.path}:${number + 1}"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            // In Elixir, the drive letter is lowercase for some reason.
            val location = "file://".length
            url = url.replaceRange(location, location + 1, url[location].toLowerCase().toString())
        }
        val state = TestStateStorage.getInstance(element.project)?.getState(url)
        return getInfo(state, isClass)
    }

    private fun getInfo(state: TestStateStorage.Record?, isClass: Boolean): Info {
        return withExecutorActions(getTestStateIcon(state, isClass))
    }

}

fun isUnderTestSources(clazz: PsiElement): Boolean {
    val psiFile = clazz.containingFile
    val vFile = psiFile.virtualFile ?: return false
    return ProjectRootManager.getInstance(clazz.project).fileIndex.isInTestSourceContent(vFile)
}