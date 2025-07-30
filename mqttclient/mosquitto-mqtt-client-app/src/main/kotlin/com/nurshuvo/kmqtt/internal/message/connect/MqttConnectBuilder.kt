package com.nurshuvo.kmqtt.internal.message.connect

import com.nurshuvo.kmqtt.internal.MqttClientConfig

class MqttConnectBuilder {

    private var keepAlive = MqttClientConfig.DEFAULT_KEEP_ALIVE
    private var reconnectDelay = MqttClientConfig.DEFAULT_MQTT_RECONNECT_DELAY_SEC
    private var sendMaximum = MqttClientConfig.DEFAULT_MQTT_SEND_MAXIMUM
    private var receiveMaximum = MqttClientConfig.DEFAULT_MQTT_RECEIVE_MAXIMUM
    private var authentication: Authentication = MqttClientConfig.DEFAULT_AUTHENTICATION

    fun setKeepALive(
        keepAlive: Int,
    ) =
        apply {
            this.keepAlive = keepAlive
        }

    fun setReconnectDelay(
        reconnectDelay: Int,
    ) =
        apply {
            this.reconnectDelay = reconnectDelay
        }

    fun setSendMaximum(
        sendMaximum: Int,
    ) =
        apply {
            this.sendMaximum = sendMaximum
        }

    fun setReceiveMaximum(
        receiveMaximum: Int,
    ) =
        apply {
            this.receiveMaximum = receiveMaximum
        }

    fun setAuthentication(
        authentication: Authentication,
    ) =
        apply {
            this.authentication = authentication
        }

    fun build() = MqttConnect(
        keepAlive,
        reconnectDelay,
        sendMaximum,
        receiveMaximum,
        authentication,
    )
}
