package com.nurshuvo.kmqtt.internal.controller.subscribe.result

import com.nurshuvo.kmqtt.internal.customerror.subscribe.SubscribeFailException
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck

sealed class MqttSubscriptionResult {
    data class Success(
        val subAck: MqttSubAck,
    ) : MqttSubscriptionResult()

    data class Failed(
        val exception: SubscribeFailException,
    ) : MqttSubscriptionResult()
}
