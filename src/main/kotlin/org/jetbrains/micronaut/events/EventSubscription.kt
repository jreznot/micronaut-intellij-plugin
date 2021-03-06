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

package org.jetbrains.micronaut.events

import com.intellij.psi.PsiAnchor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType

class EventSubscription(method: PsiMethod) {
    private val myRef: PsiAnchor = PsiAnchor.create(method)

    val method: PsiMethod?
        get() {
            return myRef.retrieve() as PsiMethod?
        }

    val eventType: PsiType?
        get() {
            val method = myRef.retrieve() as PsiMethod? ?: return null
            if (method.parameterList.parametersCount != 1) return null

            return method.parameterList.parameters[0].type
        }
}