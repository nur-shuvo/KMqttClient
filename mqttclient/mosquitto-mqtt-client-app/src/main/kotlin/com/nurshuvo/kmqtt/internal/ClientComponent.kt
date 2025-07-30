package com.nurshuvo.kmqtt.internal

import com.nurshuvo.kmqtt.internal.callbacks.MqttCallbacks
import com.nurshuvo.kmqtt.internal.handler.MqttSession
import com.nurshuvo.kmqtt.internal.handler.connect.MqttConnectHandlerImpl
import com.nurshuvo.kmqtt.internal.handler.publish.incoming.MqttIncomingPublishHandlerImpl
import com.nurshuvo.kmqtt.internal.handler.publish.outgoing.MqttOutGoingPublishHandlerImpl
import com.nurshuvo.kmqtt.internal.handler.subscribe.MqttSubscriptionHandlerImpl
import com.nurshuvo.kmqtt.internal.handler.unsubscribe.MqttUnsubscribeHandlerImpl
import com.nurshuvo.kmqtt.internal.native.ClientComponent
import com.nurshuvo.kmqtt.internal.subscriptions.ClientSubscriptions

class ClientComponent {

    val mqttCallbacks = MqttCallbacks()
    private val clientSubscriptions = ClientSubscriptions()
    val subscriptionHandler = MqttSubscriptionHandlerImpl(clientSubscriptions)

    private val incomingPublishHandler =
        MqttIncomingPublishHandlerImpl(
            clientSubscriptions = clientSubscriptions,
            mqttCallbacks = mqttCallbacks,
        )

    val outGoingPublishHandler = MqttOutGoingPublishHandlerImpl()

    private val mqttSession = MqttSession(
        subscriptionHandler = subscriptionHandler,
        outgoingPublishHandler = outGoingPublishHandler,
    )
    val connectHandler = MqttConnectHandlerImpl(mqttSession)
    val unsubscribeHandler =
        MqttUnsubscribeHandlerImpl(
            clientSubscriptions = clientSubscriptions,
            mqttCallbacks = mqttCallbacks,
        )

    val clientComponent = ClientComponent(
        connectHandler,
        outGoingPublishHandler,
        incomingPublishHandler,
        subscriptionHandler,
        unsubscribeHandler,
    )
}
