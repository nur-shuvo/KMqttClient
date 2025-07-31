package com.nurshuvo.kmqtt.internal.handler.unsubscribe


import com.nurshuvo.kmqtt.internal.MqttClientConfig.Companion.DEFAULT_MQTT_API_TIMEOUT_MS
import com.nurshuvo.kmqtt.internal.SingleRunner
import com.nurshuvo.kmqtt.internal.callbacks.MqttCallbacks
import com.nurshuvo.kmqtt.internal.constants.MOSQUITTO_API_SUCCESS_CODE
import com.nurshuvo.kmqtt.internal.constants.MOSQUITTO_API_SUCCESS_DESCRIPTOR
import com.nurshuvo.kmqtt.internal.constants.TIMEOUT_ERROR_CODE
import com.nurshuvo.kmqtt.internal.exceptions.unsubscribe.UnsubscribeFailException.TimeOutException
import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.unsubscribe.MqttUnSubAck
import com.nurshuvo.kmqtt.internal.subscriptions.ClientSubscriptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

class MqttUnsubscribeHandlerImpl(
    private val clientSubscriptions: ClientSubscriptions,
    private val mqttCallbacks: MqttCallbacks,
) : MqttUnsubscribeHandler {

    private val unsubscribeCompletableMap =
        ConcurrentHashMap<Int, CompletableDeferred<MqttUnSubAck>>()
    private val singleRunner = SingleRunner()

    override suspend fun unsubscribe(
        subscribedPublishFlowable: MqttSubscribedPublishFlowable,
    ): Result<MqttUnSubAck> {
        stopCallerFlow(subscribedPublishFlowable)

        val topic = subscribedPublishFlowable.subscribe.topic
        val clientID = subscribedPublishFlowable.clientConfig.identifier

        return if (mqttCallbacks.getCallBacksByTopic(topic).isNullOrEmpty()) {
            unsubscribeReal(topic, subscribedPublishFlowable, clientID)
        } else {
            Result.success(
                MqttUnSubAck(
                    MOSQUITTO_API_SUCCESS_CODE,
                    MOSQUITTO_API_SUCCESS_DESCRIPTOR,
                    0,
                ),
            )
        }
    }

    private suspend fun unsubscribeReal(
        topic: String,
        subscribedPublishFlowable: MqttSubscribedPublishFlowable,
        clientID: String,
    ): Result<MqttUnSubAck> {
        clientSubscriptions.remove(topic)

        val nativeClient =
            subscribedPublishFlowable.clientComponent.clientComponent
        val returnResult = nativeClient.unSubscribe(
            clientID,
            topic,
        )
        val currentMessageID = returnResult.messageID
        unsubscribeCompletableMap.computeIfAbsent(currentMessageID) { CompletableDeferred() }

        return try {
            withTimeout(DEFAULT_MQTT_API_TIMEOUT_MS) {
                unsubscribeCompletableMap[currentMessageID]?.await()?.let {
                    Result.success(it)
                } ?: Result.failure(
                    TimeOutException(
                        TIMEOUT_ERROR_CODE,
                        "Message ID $currentMessageID not found in unsubscribeCompletableMap",
                    ),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                TimeOutException(
                    TIMEOUT_ERROR_CODE,
                    "${e.message}",
                ),
            )
        } finally {
            unsubscribeCompletableMap.remove(currentMessageID)
        }
    }

    private suspend fun stopCallerFlow(subscribedPublishFlowable: MqttSubscribedPublishFlowable) {
        singleRunner.afterPrevious {
            subscribedPublishFlowable.close()
        }
    }

    override fun onUnSubAckReceived(messageId: Int) {
        unsubscribeCompletableMap.computeIfAbsent(messageId) { CompletableDeferred() }
        unsubscribeCompletableMap[messageId]?.complete(
            MqttUnSubAck(
                MOSQUITTO_API_SUCCESS_CODE,
                MOSQUITTO_API_SUCCESS_DESCRIPTOR,
                messageId,
            ),
        )
    }
}
