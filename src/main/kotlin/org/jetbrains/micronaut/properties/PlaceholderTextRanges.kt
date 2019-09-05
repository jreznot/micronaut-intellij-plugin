package org.jetbrains.micronaut.properties

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