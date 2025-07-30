package com.nurshuvo.kmqtt.internal.handler.subscribe.result

import com.nurshuvo.kmqtt.internal.exceptions.subscribe.SubscribeFailException
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck

sealed class MqttSubscriptionResult {
    data class Success(
        val subAck: MqttSubAck,
    ) : MqttSubscriptionResult()

    data class Failed(
        val exception: SubscribeFailException,
    ) : MqttSubscriptionResult()
}
