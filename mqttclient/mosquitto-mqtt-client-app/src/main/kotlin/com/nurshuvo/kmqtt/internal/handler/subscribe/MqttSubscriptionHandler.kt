package com.nurshuvo.kmqtt.internal.handler.subscribe

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.callbacks.SubscribedPublishListener
import com.nurshuvo.kmqtt.internal.handler.MqttSessionAwareHandler
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe

abstract class MqttSubscriptionHandler : MqttSessionAwareHandler() {

    abstract fun subscribe(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
        subscribe: MqttSubscribe,
        callback: SubscribedPublishListener,
    )

    abstract fun onSubAckReceived(
        messageId: Int,
        isSuccess: Boolean,
    )
}
