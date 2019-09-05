package org.jetbrains.micronaut

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod

// Example #5: Custom navigation
class MicronautLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element is PsiIdentifier) {
            if (element.parent is PsiMethod) {
                val method = element.parent as PsiMethod
                if (AnnotationUtil.isAnnotated(method, EVENT_LISTENER, 0)) {
                    return LineMarkerInfo(element, element.textRange, MicronautIcons.RECEIVER, null,
                        { _, elt -> navigateToPublisher(elt) }, GutterIconRenderer.Alignment.RIGHT
                    )
                }
            }
        }

        return null
    }

    private fun navigateToPublisher(elt: PsiElement) {
        // todo
    }
}