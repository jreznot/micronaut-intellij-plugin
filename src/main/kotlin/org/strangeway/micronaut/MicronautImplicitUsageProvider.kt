package org.strangeway.micronaut

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement

class MicronautImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitWrite(element: PsiElement?): Boolean {
        return false
    }

    override fun isImplicitRead(element: PsiElement?): Boolean {
        return false
    }

    override fun isImplicitUsage(element: PsiElement?): Boolean {
        TODO("not implemented")
    }
}