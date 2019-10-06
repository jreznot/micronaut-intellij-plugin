package org.jetbrains.micronaut

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import org.jetbrains.micronaut.events.EventSubscription
import org.jetbrains.micronaut.events.PublishEventPoint
import org.jetbrains.uast.*
import java.util.Collections.emptyList

// Example #5: Custom navigation for @EventListener
class MicronautLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>
    ) {
        val uParent = getUParentForIdentifier(element)

        if (uParent is UMethod
            && isEventListenerMethod(uParent.javaPsi)
        ) {
            // Add Line Marker for @EventListener methods

            val builder = NavigationGutterIconBuilder.create(MicronautIcons.RECEIVER)
                .setAlignment(Alignment.LEFT)
                .setTooltipText("Navigate to event publisher")
                .setTargets(getPublisherTargets(element))

            result.add(builder.createLineMarkerInfo(element))

        } else {
            val uCallExpression = element.toUElementOfType<UCallExpression>()
            if (uCallExpression != null
                && uCallExpression.kind == UastCallKind.METHOD_CALL
                && isPublishEventExpression(uCallExpression)
            ) {
                // Add Line Marker for publishEvent(event) expressions

                val sourcePsi = uCallExpression.sourcePsi
                val identifier = uCallExpression.methodIdentifier.sourcePsiElement
                if (identifier != null
                    && sourcePsi != null
                ) {
                    val builder = NavigationGutterIconBuilder.create(MicronautIcons.SENDER)
                        .setAlignment(Alignment.LEFT)
                        // Example of Lazy marker evaluation
                        .setTargets(NotNullLazyValue.createValue { findEventListeners(sourcePsi) })
                        .setTooltipText("Navigate to event listeners")

                    result.add(builder.createLineMarkerInfo(identifier))
                }
            }
        }
    }

    private fun getPublisherTargets(element: PsiElement): List<PsiMethodCallExpression> {
        if (element !is PsiIdentifier) return emptyList()
        val parent = element.parent
        if (parent !is PsiMethod) return emptyList()
        if (parent.parameterList.parametersCount != 1) return emptyList()

        val eventType = parent.parameterList.parameters[0].type
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return emptyList()

        return getPublishPoints(module, eventType)
            .filter { it.element is PsiMethodCallExpression }
            .map { it.element as PsiMethodCallExpression }
    }

    private fun findEventListeners(psiElement: PsiElement): Collection<PsiElement> {
        val module = ModuleUtilCore.findModuleForPsiElement(psiElement) ?: return emptyList()
        val uCallExpression = psiElement.toUElementOfType<UCallExpression>() ?: return emptyList()

        if (uCallExpression.valueArgumentCount != 1) return emptyList()
        val eventType = uCallExpression.valueArguments[0].getExpressionType() ?: return emptyList()

        val eventListeners = getEventListeners(module, eventType)
        val list = ArrayList<PsiElement>(eventListeners.size)
        for (listener in eventListeners) {
            val m = listener.method
            if (m != null) {
                list.add(m.navigationElement)
            }
        }
        return list
    }

    private fun getPublishPoints(module: Module, eventType: PsiType): Collection<PublishEventPoint> {
        return getPublishPointsCached(module).filter { p ->
            val pointType = p.eventType
            pointType != null && eventType.isAssignableFrom(pointType)
        }
    }

    private fun getEventListeners(module: Module, eventType: PsiType): Collection<EventSubscription> {
        return getEventListenersCached(module).filter { l ->
            val handlerType = l.eventType
            handlerType != null && eventType.isAssignableFrom(handlerType)
        }
    }

    private fun isPublishEventExpression(uMethodCall: UCallExpression): Boolean {
        if (uMethodCall.methodName == PUBLISH_EVENT_METHOD) {
            val psiMethod = uMethodCall.resolve()
            if (psiMethod != null) {
                val targetClass = psiMethod.containingClass
                return targetClass != null
                        && InheritanceUtil.isInheritor(targetClass, EVENT_PUBLISHER)
            }
        }
        return false
    }

    private fun isEventListenerMethod(psiMethod: PsiMethod): Boolean {
        return psiMethod.parameterList.parametersCount == 1
                && psiMethod.parameterList.parameters[0].type is PsiClassType
                && AnnotationUtil.isAnnotated(psiMethod, EVENT_LISTENER, 0)
    }

    private fun getPublishPointsCached(module: Module): Collection<PublishEventPoint> {
        // Example of CachedValuesManager.getCachedValue()
        val cacheManager = CachedValuesManager.getManager(module.project)

        return cacheManager.getCachedValue(module) {
            val scope = moduleWithDependenciesAndLibrariesScope(module)

            CachedValueProvider.Result.create(
                getPublishPoints(scope),
                PsiModificationTracker.MODIFICATION_COUNT,
                ProjectRootManager.getInstance(module.project)
            )
        }
    }

    // MethodReferencesSearch Usage Example
    private fun getPublishPoints(scope: GlobalSearchScope): SmartList<PublishEventPoint> {
        val points = SmartList<PublishEventPoint>()
        for (publishMethod in getPublishMethods(scope)) {
            val entries = MethodReferencesSearch.search(publishMethod, scope, true)
            for (reference in entries) {
                val expression = reference.element.getUastParentOfType<UCallExpression>()
                if (expression != null
                    && expression.valueArgumentCount == 1
                ) {
                    points.add(PublishEventPoint(expression.valueArguments[0]))
                }
            }
        }
        return points
    }

    private fun getPublishMethods(scope: GlobalSearchScope): Array<PsiMethod> {
        val javaPsiFacade = JavaPsiFacade.getInstance(scope.project)
        val publisherClass = javaPsiFacade.findClass(EVENT_PUBLISHER, scope) ?: return emptyArray()

        return publisherClass.findMethodsByName(PUBLISH_EVENT_METHOD, false)
    }

    private fun getEventListenersCached(module: Module): Collection<EventSubscription> {
        val cacheManager = CachedValuesManager.getManager(module.project)

        return cacheManager.getCachedValue(module) {
            val scope = moduleWithDependenciesAndLibrariesScope(module)

            CachedValueProvider.Result.create(
                getEventListeners(scope),
                PsiModificationTracker.MODIFICATION_COUNT,
                ProjectRootManager.getInstance(module.project)
            )
        }
    }

    private fun getEventListeners(scope: GlobalSearchScope): SmartList<EventSubscription> {
        val collector = SmartList<EventSubscription>()
        val javaPsiFacade = JavaPsiFacade.getInstance(scope.project)
        val eventListenerClass = javaPsiFacade.findClass(EVENT_LISTENER, scope)
        if (eventListenerClass != null) {
            val listenerMethods = AnnotatedElementsSearch.searchPsiMethods(eventListenerClass, scope)
            for (element in listenerMethods) {
                collector.add(EventSubscription(element))
            }
        }
        return collector
    }
}