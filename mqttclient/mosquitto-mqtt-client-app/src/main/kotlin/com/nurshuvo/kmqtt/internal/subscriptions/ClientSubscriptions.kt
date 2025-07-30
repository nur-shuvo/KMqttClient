package com.nurshuvo.kmqtt.internal.subscriptions

import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import java.util.Collections

class ClientSubscriptions {

    private val subscriptions =
        Collections.synchronizedList(mutableListOf<MqttSubscribe>())

    fun add(
        mqttSubscribe: MqttSubscribe,
    ) {
        subscriptions.add(mqttSubscribe)
    }

    fun remove(
        topic: String,
    ) {
        subscriptions.removeIf {
            it.topic == topic
        }
    }

    fun getAll(): List<MqttSubscribe> =
        subscriptions.toList()

    fun getTopics(): List<String> =
        subscriptions
            .map {
                it.topic
            }.toSet()
            .toList()
}
