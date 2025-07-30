package com.nurshuvo.kmqtt.internal.message.publish.outgoing

import com.nurshuvo.kmqtt.internal.qos.MqttQos

class MqttPublishBuilder {

    private lateinit var topic: String
    private lateinit var payload: String
    private var qos: MqttQos = MqttQos.DEFAULT
    private var retain: Boolean = false

    fun setTopic(topic: String) =
        apply {
            this.topic = topic
        }

    fun setPayload(payload: String) =
        apply {
            this.payload = payload
        }

    fun setQos(qos: MqttQos) =
        apply {
            this.qos = qos
        }

    fun setRetain(retain: Boolean) =
        apply {
            this.retain = retain
        }

    fun build(): MqttPublish {
        require(this::topic.isInitialized && this::payload.isInitialized)
        return MqttPublish(
            topic = topic,
            payload = payload,
            qos = qos,
            retain = retain,
        )
    }
}
