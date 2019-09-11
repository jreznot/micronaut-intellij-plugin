package org.jetbrains.micronaut

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import org.jetbrains.micronaut.events.EventSubscription
import org.jetbrains.micronaut.events.PublishEventPoint
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getUastParentOfType

// Example #5: Custom navigation for @EventListener
class MicronautLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>
    ) {
        if (element is PsiIdentifier
            && element.parent is PsiMethod) {
            val method = element.parent as PsiMethod
            if (AnnotationUtil.isAnnotated(method, EVENT_LISTENER, 0)) {
                val builder = NavigationGutterIconBuilder.create(MicronautIcons.RECEIVER)
                    .setTooltipText("Navigate to event publisher")
                    .setTargets(getPublisherTargets(element))

                result.add(builder.createLineMarkerInfo(element))
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

    private fun getPublishPoints(module: Module, eventType: PsiType): Collection<PublishEventPoint> {
        return getPublishPoints(module).filter { p ->
            val pointType = p.eventType
            pointType != null && eventType.isAssignableFrom(pointType)
        }
    }

    private fun getEventListeners(module: Module, eventType: PsiType): Collection<EventSubscription> {
        return getEventListeners(module).filter { l ->
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
        if (psiMethod.parameterList.parametersCount == 1
            && psiMethod.parameterList.parameters[0].type is PsiClassType) {

            if (AnnotationUtil.isAnnotated(psiMethod, EVENT_LISTENER,0)) {
                return true
            }
        }

        return false
    }

    private fun getPublishPoints(module: Module): Collection<PublishEventPoint> {
        val cacheManager = CachedValuesManager.getManager(module.project)

        return cacheManager.getCachedValue(module) {
            val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

            val points = SmartList<PublishEventPoint>()
            for (publishMethod in getPublishMethods(module, scope)) {
                val entries = MethodReferencesSearch.search(publishMethod, scope, true)
                for (reference in entries) {
                    val expression = reference.element.getUastParentOfType<UCallExpression>()
                    if (expression != null
                        && expression.valueArgumentCount == 1
                        && isPublishEventExpression(expression)) {
                        val arg = expression.valueArguments[0]
                        points.add(PublishEventPoint(arg))
                    }
                }
            }

            CachedValueProvider.Result.create(points, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    private fun getPublishMethods(module: Module, scope: GlobalSearchScope): Array<PsiMethod> {
        val javaPsiFacade = JavaPsiFacade.getInstance(module.project)
        val publisherClass = javaPsiFacade.findClass(EVENT_PUBLISHER, scope) ?: return emptyArray()

        return publisherClass.findMethodsByName(PUBLISH_EVENT_METHOD, false)
    }

    private fun getEventListeners(module: Module): Collection<EventSubscription> {
        val cacheManager = CachedValuesManager.getManager(module.project)

        return cacheManager.getCachedValue(module) {
            val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

            val javaPsiFacade = JavaPsiFacade.getInstance(module.project)
            val eventListenerClass = javaPsiFacade.findClass(EVENT_LISTENER, scope)
            val collector = SmartList<EventSubscription>()
            if (eventListenerClass != null) {
                val listenerMethods = AnnotatedElementsSearch.searchPsiMethods(eventListenerClass, scope)
                for (element in listenerMethods) {
                    collector.add(EventSubscription(element))
                }
            }

            CachedValueProvider.Result.create(collector, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }
}