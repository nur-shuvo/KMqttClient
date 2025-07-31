package com.nurshuvo.kmqtt.internal.controller.subscribe

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.callbacks.SubscribedPublishListener
import com.nurshuvo.kmqtt.internal.controller.KMqttSessionAwareHandler
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe

abstract class KMqttSubscriptionHandler : KMqttSessionAwareHandler() {

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
