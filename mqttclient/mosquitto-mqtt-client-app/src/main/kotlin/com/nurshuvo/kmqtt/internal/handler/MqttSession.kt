package com.nurshuvo.kmqtt.internal.handler

import com.nurshuvo.kmqtt.internal.handler.publish.outgoing.MqttOutGoingPublishHandler
import com.nurshuvo.kmqtt.internal.handler.subscribe.MqttSubscriptionHandler


class MqttSession(
    private val subscriptionHandler: MqttSubscriptionHandler,
    private val outgoingPublishHandler: MqttOutGoingPublishHandler,
) {
    private var hasSession = false

    fun startOrResume() {
        hasSession = true
        subscriptionHandler.onSessionStartOrResume()
        outgoingPublishHandler.onSessionStartOrResume()
    }

    fun expire(error: String) {
        if (hasSession) {
            hasSession = false
            val cause = Exception(error)
            subscriptionHandler.onSessionEnd(cause)
            outgoingPublishHandler.onSessionEnd(cause)
        }
    }
}
