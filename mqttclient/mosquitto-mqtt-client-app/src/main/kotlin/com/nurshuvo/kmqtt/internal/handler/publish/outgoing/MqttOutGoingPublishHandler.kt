package com.nurshuvo.kmqtt.internal.handler.publish.outgoing

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.handler.MqttSessionAwareHandler
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublishAck

abstract class MqttOutGoingPublishHandler : MqttSessionAwareHandler() {

    abstract suspend fun publish(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
        publish: MqttPublish,
    ): Result<MqttPublishAck>

    abstract fun onPublishAckReceived(
        messageID: Int,
    )
}
