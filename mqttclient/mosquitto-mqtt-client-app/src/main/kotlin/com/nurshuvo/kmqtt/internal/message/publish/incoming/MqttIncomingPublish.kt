package com.nurshuvo.kmqtt.internal.message.publish.incoming

import com.nurshuvo.kmqtt.internal.qos.MqttQos

class MqttIncomingPublish(
    val topic: String,
    val payload: String,
    val qos: MqttQos,
    val retain: Boolean,
)
