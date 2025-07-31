package com.nurshuvo.kmqtt.internal.controller.connect

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.message.connack.MqttConnAck

interface MqttConnectHandler {

    suspend fun connect(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
    ): Result<MqttConnAck>

    fun onConnAckReceived(
        reasonCode: Int,
        reasonDescriptor: String,
    )

    fun onDisConnAckReceived(
        reasonCode: Int,
        reasonDescriptor: String,
    )
}
