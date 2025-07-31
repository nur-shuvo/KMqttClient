package com.nurshuvo.kmqtt.internal.controller.publish.outgoing

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.MqttClientConfig.Companion.DEFAULT_MQTT_API_TIMEOUT_MS
import com.nurshuvo.kmqtt.internal.MOSQUITTO_API_SUCCESS_CODE
import com.nurshuvo.kmqtt.internal.MOSQUITTO_API_SUCCESS_DESCRIPTOR
import com.nurshuvo.kmqtt.internal.TIMEOUT_ERROR_CODE
import com.nurshuvo.kmqtt.internal.customerror.publish.PublishFailException
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublishAck
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

class KMqttOutGoingPublishHandlerImpl : KMqttOutGoingPublishHandler() {

    private val publishCompletableMap =
        ConcurrentHashMap<Int, CompletableDeferred<MqttPublishAck>>()

    override suspend fun publish(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
        publish: MqttPublish,
    ): Result<MqttPublishAck> {
        try {
            withTimeout(5000) {
                while (!hasSession) {
                    delay(100)
                }
            }
        } catch (e: TimeoutCancellationException) {
            return Result.failure(
                PublishFailException.TimeOutException(
                    TIMEOUT_ERROR_CODE,
                    "Timed out waiting for session to become active ${e.message}",
                ),
            )
        }
        val returnResult = clientComponent.clientComponent.publish(
            clientConfig.identifier,
            publish.topic,
            publish.payload,
            publish.qos.value,
            publish.retain,
        )

        val currentMessageID = returnResult.messageID
        publishCompletableMap.computeIfAbsent(currentMessageID) { CompletableDeferred() }

        if (returnResult.code != MOSQUITTO_API_SUCCESS_CODE) {
            return Result.failure(
                PublishFailException.SystemAPIException(
                    returnResult.code,
                    returnResult.descriptor,
                ),
            )
        }

        return try {
            withTimeout(DEFAULT_MQTT_API_TIMEOUT_MS) {
                publishCompletableMap[currentMessageID]?.await()?.let {
                    Result.success(it)
                } ?: Result.failure(
                    PublishFailException.TimeOutException(
                        TIMEOUT_ERROR_CODE,
                        "Message ID $currentMessageID not found in publishCompletableMap",
                    ),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                PublishFailException.TimeOutException(
                    TIMEOUT_ERROR_CODE,
                    "${e.message}",
                ),
            )
        } finally {
            publishCompletableMap.remove(currentMessageID)
        }
    }

    override fun onPublishAckReceived(
        messageID: Int,
    ) {
        publishCompletableMap.computeIfAbsent(messageID) { CompletableDeferred() }
        publishCompletableMap[messageID]?.complete(
            MqttPublishAck(
                MOSQUITTO_API_SUCCESS_CODE,
                MOSQUITTO_API_SUCCESS_DESCRIPTOR,
                messageID,
            ),
        )
    }
}
