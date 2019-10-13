package org.jetbrains.micronaut

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
                val error = validateCronExpression(cronValue)
                if (error != null) {
                    problemsHolder.registerProblem(
                        source, "Incorrect CRON expression: $error", ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
            
            val containingClass = method.containingClass
            if (containingClass != null
                && !AnnotationUtil.isAnnotated(containingClass, SINGLETON_CLASS, 0)) {

                val nameElement = annotation.nameReferenceElement
                if (nameElement != null) {
                    problemsHolder.registerProblem(nameElement, "@Scheduled can be used only inside @Singleton",
                        AddAnnotationFix(SINGLETON_CLASS, containingClass)
                    ) // or Use IntelliJ fix instead
                }
            }
            
            return problemsHolder.resultsArray
        }

        return ProblemDescriptor.EMPTY_ARRAY
    }

    private fun validateCronExpression(cron: String): String? {
        if (cron == "") return null
        try {
            CronParser(instanceDefinitionFor(CronType.SPRING)).parse(cron)
            return null
        } catch (iae: IllegalArgumentException) {
            // fallback to UNIX parser
            try {
                CronParser(instanceDefinitionFor(CronType.UNIX)).parse(cron)
                return null
            } catch (iae: IllegalArgumentException) { }

            return iae.message
        }
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