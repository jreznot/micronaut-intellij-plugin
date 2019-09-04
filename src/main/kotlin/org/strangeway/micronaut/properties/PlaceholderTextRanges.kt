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

import com.intellij.openapi.util.TextRange
import java.util.*

object PlaceholderTextRanges {

    fun getPlaceholderRanges(s: String, prefix: String, suffix: String): Set<TextRange> {
        var current = s.indexOf(prefix)
        if (current == -1) {
            return emptySet()
        }

        val ranges = LinkedHashSet<TextRange>(2)

        val prefixes = Stack<Int>()
        prefixes.push(current)
        var currentPointsAtPrefix = true

        while (current >= 0) {
            val nextSuffix = s.indexOf(suffix, if (currentPointsAtPrefix) current + prefix.length else current)
            if (nextSuffix == -1) {
                break
            }

            var nextPrefix = s.indexOf(prefix, current + 1)

            while (nextPrefix > 0 && nextPrefix + prefix.length <= nextSuffix) {
                prefixes.push(nextPrefix)
                nextPrefix = s.indexOf(prefix, nextPrefix + 1)
            }

            nextPrefix = prefixes.pop()
            val startOffset = nextPrefix + prefix.length

            val textRange = TextRange(startOffset, nextSuffix)
            ranges.add(textRange)

            while (!prefixes.isEmpty() && prefixes.peek() + prefix.length > nextPrefix) {
                prefixes.pop()
            }

            current = s.indexOf(prefix, nextSuffix + suffix.length)
            if (current > 0) {
                prefixes.push(current)
                currentPointsAtPrefix = true
            } else if (!prefixes.isEmpty()) {
                current = s.indexOf(suffix, nextSuffix + suffix.length)
                currentPointsAtPrefix = false
            }
        }

        return ranges
    }
}
