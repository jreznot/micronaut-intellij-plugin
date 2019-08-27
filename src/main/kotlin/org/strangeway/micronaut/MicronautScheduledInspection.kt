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
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue
import com.intellij.psi.PsiMethod

// Example #3 : Inspection
class MicronautScheduledInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val annotation = method.getAnnotation(SCHEDULED_METHOD)
        if (annotation != null) {
            val value = annotation.attributes
                .find { a -> a.attributeName == SCHEDULED_CRON_ATTRIBUTE }
                .let { it?.attributeValue as JvmAnnotationConstantValue? }

            val source = value?.sourceElement
            if (source != null) {
                val cronValue = value.constantValue as String
                if (!isValidCronExpression(cronValue)) {
                    return arrayOf(
                        manager.createProblemDescriptor(
                            source, "Incorrect CRON expression", isOnTheFly, null, ProblemHighlightType.GENERIC_ERROR
                        )
                    )
                }
            }
        }

        return ProblemDescriptor.EMPTY_ARRAY
    }

    private fun isValidCronExpression(cron: String): Boolean {
        if (cron == "") return true

        val cronParser = CronParser(instanceDefinitionFor(CronType.UNIX))
        try {
            cronParser.parse(cron)
        } catch (iae: IllegalArgumentException) {
            return false
        }
        return true
    }
}