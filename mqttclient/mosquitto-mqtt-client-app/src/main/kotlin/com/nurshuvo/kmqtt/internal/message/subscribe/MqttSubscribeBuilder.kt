package com.nurshuvo.kmqtt.internal.message.subscribe

import com.nurshuvo.kmqtt.internal.MqttQos

class MqttSubscribeBuilder {
    private lateinit var topicFilter: String
    private var qos: MqttQos = MqttQos.DEFAULT

    fun setTopicFilter(topicFilter: String) =
        apply {
            this.topicFilter = topicFilter
        }

    fun setQos(qos: MqttQos) =
        apply {
            this.qos = qos
        }

    fun build(): MqttSubscribe {
        require(this::topicFilter.isInitialized)
        return MqttSubscribe(
            topic = topicFilter,
            qos = qos,
        )
    }
}
