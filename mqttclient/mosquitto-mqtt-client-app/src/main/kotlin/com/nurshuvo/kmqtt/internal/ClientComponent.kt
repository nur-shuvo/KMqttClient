package com.nurshuvo.kmqtt.internal

import com.nurshuvo.kmqtt.internal.callbacks.KMqttCallbacks
import com.nurshuvo.kmqtt.internal.controller.MqttSession
import com.nurshuvo.kmqtt.internal.controller.connect.MqttConnectHandlerImpl
import com.nurshuvo.kmqtt.internal.controller.publish.incoming.MqttIncomingPublishHandlerImpl
import com.nurshuvo.kmqtt.internal.controller.publish.outgoing.KMqttOutGoingPublishHandlerImpl
import com.nurshuvo.kmqtt.internal.controller.subscribe.KMqttSubscriptionHandlerImpl
import com.nurshuvo.kmqtt.internal.controller.unsubscribe.MqttUnsubscribeHandlerImpl
import com.nurshuvo.kmqtt.internal.native.ClientComponent
import com.nurshuvo.kmqtt.internal.subscriptions.ClientSubscriptions

class ClientComponent {

    val kMqttCallbacks = KMqttCallbacks()
    private val clientSubscriptions = ClientSubscriptions()
    val subscriptionHandler = KMqttSubscriptionHandlerImpl(clientSubscriptions)

    private val incomingPublishHandler =
        MqttIncomingPublishHandlerImpl(
            clientSubscriptions = clientSubscriptions,
            kMqttCallbacks = kMqttCallbacks,
        )

    val outGoingPublishHandler = KMqttOutGoingPublishHandlerImpl()

    private val mqttSession = MqttSession(
        subscriptionHandler = subscriptionHandler,
        outgoingPublishHandler = outGoingPublishHandler,
    )
    val connectHandler = MqttConnectHandlerImpl(mqttSession)
    val unsubscribeHandler =
        MqttUnsubscribeHandlerImpl(
            clientSubscriptions = clientSubscriptions,
            kMqttCallbacks = kMqttCallbacks,
        )

    val clientComponent = ClientComponent(
        connectHandler,
        outGoingPublishHandler,
        incomingPublishHandler,
        subscriptionHandler,
        unsubscribeHandler,
    )
}
