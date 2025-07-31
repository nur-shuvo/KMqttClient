package com.nurshuvo.kmqtt.internal.handler.unsubscribe

import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.unsubscribe.MqttUnSubAck

interface MqttUnsubscribeHandler {

    suspend fun unsubscribe(
        subscribedPublishFlowable: MqttSubscribedPublishFlowable,
    ): Result<MqttUnSubAck>

    fun onUnSubAckReceived(
        messageId: Int,
    )
}
