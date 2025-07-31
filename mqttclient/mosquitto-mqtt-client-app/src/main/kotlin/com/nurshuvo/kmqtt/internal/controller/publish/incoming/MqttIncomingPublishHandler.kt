package com.nurshuvo.kmqtt.internal.controller.publish.incoming

import com.nurshuvo.kmqtt.internal.MqttQos

interface MqttIncomingPublishHandler {

    fun onMessageArrived(
        topic: String,
        payload: String,
        qos: MqttQos,
        retain: Boolean,
    )
}
