package com.nurshuvo.kmqtt.internal.message.publish.incoming

import com.nurshuvo.kmqtt.internal.qos.MqttQos

class MqttIncomingPublish<E>(
    val topic: String,
    val payload: E,
    val qos: MqttQos,
    val retain: Boolean,
)
