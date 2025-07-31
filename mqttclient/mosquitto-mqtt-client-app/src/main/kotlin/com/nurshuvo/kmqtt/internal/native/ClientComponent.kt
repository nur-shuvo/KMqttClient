package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.controller.connect.MqttConnectHandler
import com.nurshuvo.kmqtt.internal.controller.publish.incoming.MqttIncomingPublishHandler
import com.nurshuvo.kmqtt.internal.controller.publish.outgoing.KMqttOutGoingPublishHandler
import com.nurshuvo.kmqtt.internal.controller.subscribe.KMqttSubscriptionHandler
import com.nurshuvo.kmqtt.internal.controller.unsubscribe.MqttUnsubscribeHandler
import com.nurshuvo.kmqtt.internal.MqttQos

@Keep
class ClientComponent(
    private val connectHandler: MqttConnectHandler,
    private val outGoingPublishHandler: KMqttOutGoingPublishHandler,
    private val incomingPublishHandler: MqttIncomingPublishHandler,
    private val subscriptionHandler: KMqttSubscriptionHandler,
    private val unsubscribeHandler: MqttUnsubscribeHandler,
) : NativeClientComponent() {

    override fun onConnectEvent(
        reasonCode: Int,
        reasonDescriptor: String,
    ) {
        connectHandler.onConnAckReceived(
            reasonCode,
            reasonDescriptor,
        )
    }

    override fun onDisconnectEvent(
        reasonCode: Int,
        reasonDescriptor: String,
    ) {
        connectHandler.onDisConnAckReceived(
            reasonCode,
            reasonDescriptor,
        )
    }

    override fun onMessageEvent(
        mid: Int,
        topic: String,
        len: Int,
        payload: String,
        qos: Int,
        retain: Boolean,
    ) {
        incomingPublishHandler.onMessageArrived(
            topic,
            payload,
            MqttQos.valueOf(qos),
            retain,
        )
    }

    override fun onSubscribeEvent(
        mid: Int,
        isSuccess: Boolean,
    ) {
        subscriptionHandler.onSubAckReceived(
            mid,
            isSuccess,
        )
    }

    override fun onUnsubscribeEvent(
        mid: Int,
    ) {
        unsubscribeHandler.onUnSubAckReceived(mid)
    }

    override fun onPublishEvent(
        mid: Int,
    ) {
        outGoingPublishHandler.onPublishAckReceived(mid)
    }
}
