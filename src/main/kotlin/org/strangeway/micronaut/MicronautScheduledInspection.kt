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

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor
import com.cronutils.parser.CronParser
import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInspection.*
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*

// Example #3 : Inspection
class MicronautScheduledInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val annotation = method.getAnnotation(SCHEDULED_METHOD)
        if (annotation != null) {
            val containingFile = method.containingFile
            val problemsHolder = ProblemsHolder(manager, containingFile, isOnTheFly)

            val value = annotation.attributes
                .find { a -> a.attributeName == SCHEDULED_CRON_ATTRIBUTE }
                .let { it?.attributeValue as JvmAnnotationConstantValue? }

            val source = value?.sourceElement
            if (source != null) {
                val cronValue = value.constantValue as String
                if (!isValidCronExpression(cronValue)) {
                    problemsHolder.registerProblem(
                        source, "Incorrect CRON expression", ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
            
            val containingClass = method.containingClass
            if (containingClass != null
                && !AnnotationUtil.isAnnotated(containingClass, SINGLETON_CLASS, 0)) {

                val nameElement = annotation.nameReferenceElement
                if (nameElement != null) {
                    problemsHolder.registerProblem(nameElement, "@Scheduled can be used only inside @Singleton",
                        AddAnnotationFix(SINGLETON_CLASS, containingClass)) // or Use IntelliJ fix instead
                }
            }
            
            return problemsHolder.resultsArray
        }

        return ProblemDescriptor.EMPTY_ARRAY
    }

    private fun isValidCronExpression(cron: String): Boolean {
        if (cron == "") return true

        val cronParser = CronParser(instanceDefinitionFor(CronType.QUARTZ))
        try {
            cronParser.parse(cron)
        } catch (iae: IllegalArgumentException) {
            return false
        }
        return true
    }

    class AddAnnotationFix(private val fqn: String, psiClass : PsiClass) : LocalQuickFixOnPsiElement(psiClass) {
        override fun getFamilyName(): String = "Annotate class with @Singleton"

        override fun getText(): String = familyName

        override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
            val psiClass = startElement as PsiModifierListOwner
            val containingFile = psiClass.containingFile

            WriteCommandAction.runWriteCommandAction(project, "Annotate class with @Singleton", null, Runnable {
                psiClass.modifierList?.addAnnotation(fqn)
            }, containingFile)
        }
    }
}