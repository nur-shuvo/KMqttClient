package com.nurshuvo.kmqtt.internal.controller

import com.nurshuvo.kmqtt.internal.controller.publish.outgoing.KMqttOutGoingPublishHandler
import com.nurshuvo.kmqtt.internal.controller.subscribe.KMqttSubscriptionHandler

class MqttSession(
    private val subscriptionHandler: KMqttSubscriptionHandler,
    private val outgoingPublishHandler: KMqttOutGoingPublishHandler,
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
