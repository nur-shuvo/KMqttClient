package com.nurshuvo.kmqtt.internal

class MqttClientBuilder {
    private var identifier = MqttClientConfig.DEFAULT_CLIENT_IDENTIFIER
    private var serverHost = MqttClientConfig.DEFAULT_SERVER_HOST
    private var serverPort = MqttClientConfig.DEFAULT_SERVER_PORT
    private var cleanSession = MqttClientConfig.DEFAULT_CLEAN_SESSION

    fun setIdentifier(
        identifier: String,
    ) = apply {
        this.identifier = identifier
    }

    fun setServerHost(
        serverHost: String,
    ) = apply {
        this.serverHost = serverHost
    }

    fun setServerPort(
        serverPort: Int,
    ) = apply {
        this.serverPort = serverPort
    }

    fun setCleanSession(
        cleanSession: Boolean,
    ) = apply {
        this.cleanSession = cleanSession
    }

    fun build(): MqttClient {
        return MqttClient(buildClientConfig())
    }

    private fun buildClientConfig(): MqttClientConfig {
        return MqttClientConfig(
            identifier = identifier,
            serverHost = serverHost,
            serverPort = serverPort,
            cleanSession = cleanSession,
        )
    }
}
