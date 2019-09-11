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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.getUCallExpression

class PublishEventPoint(private val myExpression: UExpression) {
    val eventType: PsiType?
        get() {
            return myExpression.getExpressionType()
        }

    val element: PsiElement?
        get() {
            val callExpression = myExpression.uastParent.getUCallExpression() ?: return myExpression.sourcePsi
            val sourcePsi = callExpression.sourcePsi ?: return myExpression.sourcePsi
            return sourcePsi
        }
}