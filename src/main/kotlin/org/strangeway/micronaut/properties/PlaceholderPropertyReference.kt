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

package org.strangeway.micronaut.properties

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.lang.properties.references.PropertiesPsiCompletionUtil
import com.intellij.lang.properties.references.PropertyReferenceBase
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import java.util.*
import kotlin.collections.HashSet

class PlaceholderPropertyReference private constructor(
    key: String, element: PsiElement, textRange: TextRange
) : PropertyReferenceBase(key, true, element, textRange), LocalQuickFixProvider {

    override fun getQuickFixes(): Array<LocalQuickFix>? {
        return null
    }

    override fun getVariants(): Array<Any> {
        val variants = HashSet<Any>()
        propertiesFiles?.forEach {
            PropertiesPsiCompletionUtil.addVariantsFromFile(this, it, variants)
        }

        return variants.toArray()
    }

    override fun getPropertiesFiles(): List<PropertiesFile>? {
        val propertiesFiles = ArrayList<PropertiesFile>()
        val module = ModuleUtilCore.findModuleForPsiElement(element)

        if (module != null) {
            val directories = getConfigDirectories(module)
            for (directory in directories) {
                val files = directory.files
                for (file in files) {
                    if (file is PropertiesFile && file.getName().startsWith("application")) {
                        propertiesFiles.add(file as PropertiesFile)
                    }
                }
            }
        }

        return propertiesFiles
    }

    companion object {

        fun create(element: PsiElement, textRange: TextRange, info: PlaceholderInfo): PlaceholderPropertyReference {
            val text = info.text
            if (text.contains(":")) {
                val offset = textRange.startOffset
                val endOffset = text.indexOf(":")

                val key = text.substring(0, endOffset)
                return PlaceholderPropertyReference(
                    key, element, TextRange.from(offset, endOffset)
                )
            }
            return PlaceholderPropertyReference(text, element, textRange)
        }

        private fun getConfigDirectories(module: Module): Set<PsiDirectory> {
            val configs = HashSet<PsiDirectory>()

            collectConfigDirectories(module, configs)

            return configs
        }

        private fun collectConfigDirectories(
            module: Module,
            configs: MutableSet<in PsiDirectory>,
            visitedModules: MutableSet<in Module> = HashSet()
        ) {
            if (visitedModules.contains(module)) {
                return
            }

            visitedModules.add(module)

            val moduleRootManager = ModuleRootManager.getInstance(module)
            val entries = moduleRootManager.contentEntries

            for (entry in entries) {
                val folders = entry.sourceFolders
                for (folder in folders) {
                    val root = folder.file
                    if (root != null && root.isDirectory) {
                        val directory = PsiManager.getInstance(module.project).findDirectory(root)
                        if (directory != null) {
                            configs.add(directory)
                        }
                    }
                }
            }

            for (dependentModule in moduleRootManager.dependencies) {
                collectConfigDirectories(dependentModule, configs, visitedModules)
            }
        }
    }
}
