package com.nurshuvo.kmqtt.internal.handler.publish.incoming

import com.nurshuvo.kmqtt.internal.qos.MqttQos

interface MqttIncomingPublishHandler {

    fun onMessageArrived(
        topic: String,
        payload: String,
        qos: MqttQos,
        retain: Boolean,
    )
}
