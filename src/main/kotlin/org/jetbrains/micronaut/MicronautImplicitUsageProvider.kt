package org.jetbrains.micronaut

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.*

// Example #2 : Implicit entry points of the framework
class MicronautImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitWrite(element: PsiElement?): Boolean {
        return element is PsiField
                && AnnotationUtil.isAnnotated(element, INJECT, 0)
    }

    override fun isImplicitRead(element: PsiElement?): Boolean {
        return false
    }

    override fun isImplicitUsage(element: PsiElement?): Boolean {
        if (element is PsiClass) {
            return AnnotationUtil.isAnnotated(element, BEAN_ANNOTATIONS, 0)
        }
        if (element is PsiMethod) {
            return element.hasModifierProperty(PsiModifier.PUBLIC)
                    && !element.hasModifierProperty(PsiModifier.STATIC)
                    && AnnotationUtil.isAnnotated(element, METHOD_ANNOTATIONS, 0)
                    && inControllerClass(element)
        }
        return false
    }

    private fun inControllerClass(element: PsiMethod): Boolean {
        val containingClass = element.containingClass
        return containingClass != null
                && containingClass.qualifiedName != null
                && AnnotationUtil.isAnnotated(containingClass, CONTROLLER_CLASS, 0)
    }
}