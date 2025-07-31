package com.nurshuvo.kmqtt.internal.controller.publish.outgoing

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.controller.KMqttSessionAwareHandler
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublishAck

abstract class KMqttOutGoingPublishHandler : KMqttSessionAwareHandler() {

    abstract suspend fun publish(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
        publish: MqttPublish,
    ): Result<MqttPublishAck>

    abstract fun onPublishAckReceived(
        messageID: Int,
    )
}
