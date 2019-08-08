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

import com.intellij.compiler.CompilerConfiguration
import com.intellij.compiler.CompilerConfigurationImpl
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.awt.RelativePoint
import javax.swing.event.HyperlinkEvent

// Example #1 : Check configuration and show notification on project start
class CheckAnnotationProcessorsStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        DumbService.getInstance(project).runWhenSmart {
            // after initial indexing

            val javaPsiFacade = JavaPsiFacade.getInstance(project)
            val projectScope = GlobalSearchScope.allScope(project)

            if (javaPsiFacade.findClass(APPLICATION_CLASS, projectScope) != null) {
                // Yey! we have Micronaut in classpath

                val compilerConfiguration = getCompilerConfiguration(project)
                if (!compilerConfiguration.defaultProcessorProfile.isEnabled) {
                    suggestEnableAnnotations(project)
                }
            }
        }
    }

    private fun getCompilerConfiguration(project: Project) =
        CompilerConfiguration.getInstance(project) as CompilerConfigurationImpl

    private fun suggestEnableAnnotations(project: Project) {
        val statusBar = WindowManager.getInstance().getStatusBar(project)

        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                "Do you want to enable annotation processors for Micronaut compilation? <a href=\"enable\">Enable</a>",
                MessageType.WARNING
            ) { e ->
                if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                    enableAnnotations(project)
                }
            }
            .setHideOnLinkClick(true)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)
    }

    private fun enableAnnotations(project: Project) {
        getCompilerConfiguration(project).defaultProcessorProfile.isEnabled = true

        val statusBar = WindowManager.getInstance().getStatusBar(project)
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(
                "Java annotation processing has been enabled",
                MessageType.INFO,
                null
            )
            .setFadeoutTime(3000)
            .createBalloon()
            .show(RelativePoint.getNorthEastOf(statusBar.component), Balloon.Position.atRight)
    }
}