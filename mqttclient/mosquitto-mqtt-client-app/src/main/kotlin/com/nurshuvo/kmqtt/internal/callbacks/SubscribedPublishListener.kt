package com.nurshuvo.kmqtt.internal.callbacks

import com.nurshuvo.kmqtt.internal.exceptions.subscribe.SubscribeFailException
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck

interface SubscribedPublishListener {
    fun onSuccess(
        subAck: MqttSubAck,
    )

    fun onFailure(
        exception: SubscribeFailException,
    )

    fun onMessageReceived(
        mqttPublish: MqttPublish,
    )
}
