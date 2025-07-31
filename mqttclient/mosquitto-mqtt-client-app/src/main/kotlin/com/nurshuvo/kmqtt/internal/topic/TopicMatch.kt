package com.nurshuvo.kmqtt.internal.topic

object TopicMatch {

    fun isMatched(topicFilter: String, topicName: String): Boolean {
        // Quick check for exact match
        if (topicFilter == topicName) return true

        var topicIndex = 0
        var filterIndex = 0
        val topicLength = topicName.length
        val filterLength = topicFilter.length

        while (filterIndex < filterLength && topicIndex < topicLength) {
            val filterChar = topicFilter[filterIndex]
            val topicChar = topicName[topicIndex]

            // '#' matches all remaining characters
            if (filterChar == '#') {
                return true
            }

            // '/' must match exactly
            if (topicChar == '/' && filterChar != '/') {
                return false
            }

            // If not '+', '#', or exact match, return false
            if (filterChar != '+' && filterChar != '#' && filterChar != topicChar) {
                return false
            }

            // Handle '+' wildcard: match until next '/'
            if (filterChar == '+') {
                topicIndex++
                while (topicIndex < topicLength && topicName[topicIndex] != '/') {
                    topicIndex++
                }
                filterIndex++
                continue
            }

            topicIndex++
            filterIndex++
        }

        // Exact end match
        if (topicIndex == topicLength && filterIndex == filterLength) {
            return true
        }

        // Handle trailing "/#" pattern
        if (topicIndex == topicLength && filterIndex < filterLength) {
            if (topicName.getOrNull(topicIndex - 1) == '/' && topicFilter[filterIndex] == '#') {
                return true
            }

            if (filterIndex + 1 < filterLength &&
                topicFilter.substring(filterIndex, filterIndex + 2) == "/#"
            ) {
                return true
            }
        }

        return false
    }
}
