package com.nurshuvo.kmqtt.internal.matchtopic

class TopicMatch {
    companion object {
        fun isMatched(
            topicFilter: String,
            topicName: String,
        ): Boolean {
            var topicPosition = 0
            var filterPosition = 0
            val topicLength = topicName.length
            val filterLength = topicFilter.length

            if (topicFilter == topicName) {
                return true
            }

            while (filterPosition < filterLength &&
                topicPosition < topicLength
            ) {
                if (topicFilter[filterPosition] == '#') {
                    topicPosition = topicLength
                    filterPosition = filterLength
                    break
                }
                if (topicName[topicPosition] == '/' &&
                    topicFilter[filterPosition] != '/'
                ) {
                    break
                }

                if (topicFilter[filterPosition] != '+' &&
                    topicFilter[filterPosition] != '#' &&
                    topicFilter[filterPosition] != topicName[topicPosition]
                ) {
                    break
                }

                if (topicFilter[filterPosition] == '+') {
                    var nextPosition = topicPosition + 1
                    while (nextPosition < topicLength &&
                        topicName[nextPosition] != '/'
                        ) nextPosition = ++topicPosition + 1
                }

                filterPosition++
                topicPosition++
            }

            if ((topicPosition == topicLength) &&
                (filterPosition == filterLength)
            ) {
                return true
            } else {
                if ((topicFilter.length - filterPosition > 0) &&
                    (topicPosition == topicLength)
                ) {
                    if (topicName[topicPosition - 1] == '/' &&
                        topicFilter[filterPosition] == '#'
                    ) {
                        return true
                    }
                    if (topicFilter.length - filterPosition > 1 &&
                        topicFilter.substring(
                            filterPosition,
                            filterPosition + 2,
                        ) == "/#"
                    ) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
