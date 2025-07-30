package com.nurshuvo.kmqtt.internal.message.connect

class MqttConnect(
    val keepAlive: Int,
    val reconnectDelay: Int,
    val sendMaximum: Int,
    val receiveMaximum: Int,
    val authentication: Authentication,
) {
    companion object {
        fun builder() = MqttConnectBuilder()
    }
}
