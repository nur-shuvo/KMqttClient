package com.nurshuvo.kmqtt.internal.callbacks

import java.util.concurrent.ConcurrentHashMap

class KMqttCallbacks {

    private val callbacksByTopic =
        ConcurrentHashMap<String, MutableList<ConcurrentHashMap<Int, SubscribedPublishListener>>>()
    private val callbackByMessageId =
        ConcurrentHashMap<Int, SubscribedPublishListener>()

    fun registerCallback(
        topic: String,
        messageId: Int,
        callback: SubscribedPublishListener,
    ) {
        callbacksByTopic.compute(topic) { _, topicCallbacks ->
            val updatedCallbacks =
                topicCallbacks ?: mutableListOf()
            synchronized(updatedCallbacks) {
                val messageCallback = ConcurrentHashMap<Int, SubscribedPublishListener>()
                messageCallback[messageId] = callback
                updatedCallbacks.add(messageCallback)
            }
            updatedCallbacks
        }
        callbackByMessageId[messageId] = callback
    }

    fun unRegisterCallback(
        topic: String,
        callback: SubscribedPublishListener,
    ) {
        callbacksByTopic.computeIfPresent(topic) { _, topicCallbacks ->
            synchronized(topicCallbacks) {
                topicCallbacks.removeIf { it.values.contains(callback) }
                if (topicCallbacks.isEmpty()) null else topicCallbacks
            }
        }
        callbackByMessageId.entries.removeIf { it.value == callback }
    }

    fun getCallbackByMessageId(messageId: Int): SubscribedPublishListener? {
        return callbackByMessageId[messageId]
    }

    fun getCallBacksByTopic(topic: String) =
        callbacksByTopic[topic]?.toList()
}
