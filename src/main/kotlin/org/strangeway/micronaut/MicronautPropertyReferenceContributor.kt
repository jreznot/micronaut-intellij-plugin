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

import org.strangeway.micronaut.properties.PlaceholderTextRanges
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.SmartList
import gnu.trove.THashMap
import org.strangeway.micronaut.properties.PlaceholderInfo
import org.strangeway.micronaut.properties.PlaceholderPropertyReference
import java.util.Collections.singletonMap

// Example #6: Reference to property definition from String literal
class MicronautPropertyReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val injection = injectionHostUExpression()

        registrar.registerUastReferenceProvider(
            injection.annotationParam(VALUE, "value"),
            uastInjectionHostReferenceProvider { _, host -> createPlaceholderPropertiesReferences(host) },
            PsiReferenceRegistrar.HIGHER_PRIORITY
        )

        registrar.registerUastReferenceProvider(
            injection.annotationParam(PROPERTY, "name"),
            uastInjectionHostReferenceProvider { _, host -> createPropertyReferences(host) },
            PsiReferenceRegistrar.HIGHER_PRIORITY
        )
    }

    private fun createPropertyReferences(host: PsiLanguageInjectionHost): Array<PsiReference> {
        return createPlaceholderPropertiesReferences(getFullTextRange(host), host)
    }

    private fun createPlaceholderPropertiesReferences(host: PsiLanguageInjectionHost): Array<PsiReference> {
        return createPlaceholderPropertiesReferences(getTextRanges(host), host)
    }

    private fun createPlaceholderPropertiesReferences(
        textRanges: Map<TextRange, PlaceholderInfo>,
        valueElement: PsiElement?
    ): Array<PsiReference> {
        if (valueElement == null || textRanges.isEmpty()) return PsiReference.EMPTY_ARRAY

        val references = SmartList<PsiReference>()

        for ((textRange, info) in textRanges) {
            references.add(PlaceholderPropertyReference.create(valueElement, textRange, info))
        }
        return references.toTypedArray()
    }

    private fun getTextRanges(element: PsiElement): Map<TextRange, PlaceholderInfo> {
        val textRanges = THashMap<TextRange, PlaceholderInfo>()
        val text = element.text

        val ranges = PlaceholderTextRanges.getPlaceholderRanges(text, "\${", "}")
        for (textRange in ranges) {
            val placeholderText = textRange.substring(text)
            textRanges[textRange] = PlaceholderInfo(placeholderText)
        }
        return textRanges
    }

    private fun getFullTextRange(element: PsiElement): Map<TextRange, PlaceholderInfo> {
        val textRange = TextRange(1, element.text.length - 1)
        val text = element.text
        val placeholderText = textRange.substring(text)
        return singletonMap(textRange, PlaceholderInfo(placeholderText))
    }
}