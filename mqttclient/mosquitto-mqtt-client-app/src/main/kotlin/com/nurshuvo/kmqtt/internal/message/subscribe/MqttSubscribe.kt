package com.nurshuvo.kmqtt.internal.message.subscribe

import com.nurshuvo.kmqtt.internal.qos.MqttQos

class MqttSubscribe(
    val topic: String,
    val qos: MqttQos,
) {
    companion object {
        fun builder() = MqttSubscribeBuilder()
    }
}
