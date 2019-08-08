/*
 *  Copyright (c) 2008-2016 StrangeWayOrg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.strangeway.micronaut

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier

// Example #2 : Implicit entry points of the framework
class MicronautImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitWrite(element: PsiElement?): Boolean {
        return false
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