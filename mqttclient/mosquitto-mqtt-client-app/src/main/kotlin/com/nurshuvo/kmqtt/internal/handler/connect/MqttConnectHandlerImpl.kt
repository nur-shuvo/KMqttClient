package com.nurshuvo.kmqtt.internal.handler.connect

import android.util.Log
import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.ControlledRunner
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.MqttClientConfig.Companion.DEFAULT_MQTT_API_TIMEOUT_MS
import com.nurshuvo.kmqtt.internal.constants.MOSQUITTO_API_SUCCESS_CODE
import com.nurshuvo.kmqtt.internal.constants.TIMEOUT_ERROR_CODE
import com.nurshuvo.kmqtt.internal.exceptions.connect.ConnectionFailException
import com.nurshuvo.kmqtt.internal.handler.MqttSession
import com.nurshuvo.kmqtt.internal.message.connack.MqttConnAck
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class MqttConnectHandlerImpl(
    private val mqttSession: MqttSession,
) : MqttConnectHandler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var ackDeferred: CompletableDeferred<MqttConnAck>? = null
    private val controlledRunner = ControlledRunner<Result<MqttConnAck>>()

    override suspend fun connect(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
    ): Result<MqttConnAck> {
        return controlledRunner.joinPreviousOrRun {
            val returnResult = clientComponent.clientComponent.connect(clientConfig)
            if (returnResult.code != MOSQUITTO_API_SUCCESS_CODE) {
                return@joinPreviousOrRun Result.failure(
                    ConnectionFailException.SystemAPIException(
                        returnResult.code,
                        returnResult.descriptor,
                    ),
                )
            }

            ackDeferred = CompletableDeferred()

            try {
                withTimeout(DEFAULT_MQTT_API_TIMEOUT_MS) {
                    val ack = ackDeferred?.await() as MqttConnAck
                    if (ack.code != MOSQUITTO_API_SUCCESS_CODE) {
                        Result.failure(
                            ConnectionFailException.BrokerRejectionException(
                                ack.code,
                                ack.descriptor,
                            ),
                        )
                    } else {
                        Result.success(ack)
                    }
                }
            } catch (_: Exception) {
                Result.failure(
                    ConnectionFailException.TimeOutException(
                        TIMEOUT_ERROR_CODE,
                        "Connect timeout",
                    ),
                )
            } finally {
                ackDeferred = null
            }
        }
    }

    override fun onConnAckReceived(
        reasonCode: Int,
        reasonDescriptor: String,
    ) {
        scope.launch {
            ackDeferred?.complete(MqttConnAck(reasonCode, reasonDescriptor))
            if (reasonCode == MOSQUITTO_API_SUCCESS_CODE) {
                mqttSession.startOrResume()
            }
        }
    }

    override fun onDisConnAckReceived(
        reasonCode: Int,
        reasonDescriptor: String,
    ) {
        mqttSession.expire(reasonDescriptor)
    }
}
