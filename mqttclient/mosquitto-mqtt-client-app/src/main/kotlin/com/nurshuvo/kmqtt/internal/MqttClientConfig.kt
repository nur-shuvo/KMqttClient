package com.nurshuvo.kmqtt.internal

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CarefulFieldForJNI
import com.nurshuvo.kmqtt.internal.message.connect.Authentication
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect

@CarefulRenameClassForJNI
@Keep
data class MqttClientConfig(
    @CarefulFieldForJNI var identifier: String = DEFAULT_CLIENT_IDENTIFIER,
    @CarefulFieldForJNI var serverHost: String = DEFAULT_SERVER_HOST,
    @CarefulFieldForJNI var serverPort: Int = DEFAULT_SERVER_PORT,
    @CarefulFieldForJNI var cleanSession: Boolean = DEFAULT_CLEAN_SESSION,
    @CarefulFieldForJNI var keepAlive: Int = DEFAULT_KEEP_ALIVE,
    @CarefulFieldForJNI var reconnectDelay: Int = DEFAULT_MQTT_RECONNECT_DELAY_SEC,
    @CarefulFieldForJNI var sendMaximum: Int = DEFAULT_MQTT_SEND_MAXIMUM,
    @CarefulFieldForJNI var receiveMaximum: Int = DEFAULT_MQTT_RECEIVE_MAXIMUM,
    @CarefulFieldForJNI var authentication: Authentication = DEFAULT_AUTHENTICATION,
) {

    companion object {
        const val DEFAULT_SERVER_HOST = "localhost"
        const val DEFAULT_CLIENT_IDENTIFIER = ""
        const val DEFAULT_SERVER_PORT = 1883
        const val DEFAULT_CLEAN_SESSION = false
        const val DEFAULT_KEEP_ALIVE = 60
        const val DEFAULT_MQTT_RECONNECT_DELAY_SEC = 1
        const val DEFAULT_MQTT_SEND_MAXIMUM = 20
        const val DEFAULT_MQTT_RECEIVE_MAXIMUM = 20
        const val DEFAULT_MQTT_API_TIMEOUT_MS = 10_000L
        val DEFAULT_AUTHENTICATION = Authentication.NoAuthentication
    }

    fun setConnectConfig(
        connect: MqttConnect,
    ) = this.apply {
        keepAlive = connect.keepAlive
        reconnectDelay = connect.reconnectDelay
        sendMaximum = connect.sendMaximum
        receiveMaximum = connect.receiveMaximum
        authentication = connect.authentication
    }
}
