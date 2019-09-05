package org.jetbrains.micronaut

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import org.jetbrains.uast.UField

// Example #4: UAST inspection for Java/Kotlin/etc
class MicronautFieldInjectionInspection : AbstractBaseUastLocalInspectionTool(UField::class.java) {
    override fun checkField(field: UField, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val inject = field.findAnnotation(INJECT)
        if (inject != null) {
            val sourcePsi = field.uastAnchor?.sourcePsi // try to get identifier element and its PSI source
            if (sourcePsi != null) {
                return arrayOf(manager.createProblemDescriptor(
                    sourcePsi, "Field injection is not recommended!", isOnTheFly,
                    emptyArray(), ProblemHighlightType.WARNING))
            }
        }
        return ProblemDescriptor.EMPTY_ARRAY
    }
}