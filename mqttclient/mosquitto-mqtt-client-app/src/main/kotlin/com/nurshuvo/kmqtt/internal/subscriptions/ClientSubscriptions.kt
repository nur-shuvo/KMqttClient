package com.nurshuvo.kmqtt.internal.subscriptions

import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import java.util.Collections

class ClientSubscriptions {

    private val subscriptionList = Collections.synchronizedList(mutableListOf<MqttSubscribe>())

    fun add(subscription: MqttSubscribe) {
        subscriptionList.add(subscription)
    }

    fun remove(topic: String) {
        subscriptionList.removeIf { it.topic == topic }
    }

    fun getAll(): List<MqttSubscribe> = subscriptionList.toList()

    fun getTopics(): List<String> =
        subscriptionList
            .map { it.topic }
            .distinct()
}
