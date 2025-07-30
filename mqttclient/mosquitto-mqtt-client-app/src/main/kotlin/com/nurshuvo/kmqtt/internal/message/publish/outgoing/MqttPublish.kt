package com.nurshuvo.kmqtt.internal.message.publish.outgoing

import com.nurshuvo.kmqtt.internal.qos.MqttQos

class MqttPublish(
    val topic: String,
    val payload: String,
    val qos: MqttQos,
    val retain: Boolean,
) {
    companion object {
        fun builder() = MqttPublishBuilder()
    }
}
