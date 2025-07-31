package com.nurshuvo.kmqtt.internal

import com.nurshuvo.kmqtt.internal.constants.INVALID_FLOWABLE_TYPE
import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.flowable.FlowableWithSingle
import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.connack.MqttConnAck
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublishAck
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import com.nurshuvo.kmqtt.internal.message.unsubscribe.MqttUnSubAck

class MqttClient private constructor() {

    lateinit var clientConfig: MqttClientConfig
    val clientComponent = ClientComponent()

    constructor(
        clientConfig: MqttClientConfig,
    ) : this() {
        this.clientConfig = clientConfig
    }

    companion object {
        fun builder() = MqttClientBuilder()

        init {
            System.loadLibrary("mosquitto_client_android")
        }
    }

    suspend fun connect(
        mqttConnect: MqttConnect,
    ): Result<MqttConnAck> {
        clientConfig.setConnectConfig(mqttConnect)
        val connectHandler = clientComponent.connectHandler
        return connectHandler.connect(clientConfig, clientComponent)
    }

    fun subscribe(
        mqttSubscribe: MqttSubscribe,
    ) = MqttSubscribedPublishFlowable(
        clientConfig,
        clientComponent,
        mqttSubscribe,
    )

    suspend fun publish(
        mqttPublish: MqttPublish,
    ): Result<MqttPublishAck> {
        val outGoingPublishHandler = clientComponent.outGoingPublishHandler
        return outGoingPublishHandler.publish(
            clientConfig,
            clientComponent,
            mqttPublish,
        )
    }

    suspend fun unSubscribe(
        flowable: FlowableWithSingle<*, *>,
    ): Result<MqttUnSubAck> {
        val unsubscribeHandler = clientComponent.unsubscribeHandler

        return if (flowable is MqttSubscribedPublishFlowable) {
            unsubscribeHandler.unsubscribe(flowable)
        } else {
            Result.failure(IllegalArgumentException(INVALID_FLOWABLE_TYPE))
        }
    }
}
