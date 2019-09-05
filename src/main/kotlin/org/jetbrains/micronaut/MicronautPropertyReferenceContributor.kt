package org.jetbrains.micronaut

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.SmartList
import gnu.trove.THashMap
import org.jetbrains.micronaut.properties.PlaceholderInfo
import org.jetbrains.micronaut.properties.PlaceholderPropertyReference
import org.jetbrains.micronaut.properties.PlaceholderTextRanges
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
        val text = ElementManipulators.getValueText(element)
        val textRange = ElementManipulators.getValueTextRange(element)
        return singletonMap(textRange, PlaceholderInfo(text))
    }
}