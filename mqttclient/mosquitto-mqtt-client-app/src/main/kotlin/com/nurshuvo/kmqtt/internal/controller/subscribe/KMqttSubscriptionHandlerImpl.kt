package com.nurshuvo.kmqtt.internal.controller.subscribe

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.concurrency.ControlledRunner
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.callbacks.SubscribedPublishListener
import com.nurshuvo.kmqtt.internal.MOSQUITTO_API_SUCCESS_CODE
import com.nurshuvo.kmqtt.internal.MOSQUITTO_API_SUCCESS_DESCRIPTOR
import com.nurshuvo.kmqtt.internal.SUBSCRIBE_REJECT_BY_BROKER_ERROR_CODE
import com.nurshuvo.kmqtt.internal.SUBSCRIBE_REJECT_BY_BROKER_ERR_DESCRIPTION
import com.nurshuvo.kmqtt.internal.customerror.subscribe.SubscribeFailException
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import com.nurshuvo.kmqtt.internal.subscriptions.ClientSubscriptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class KMqttSubscriptionHandlerImpl(
    private val clientSubscriptions: ClientSubscriptions,
) : KMqttSubscriptionHandler() {

    private val controlledRunner = ControlledRunner<Unit>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var clientComponent: ClientComponent
    private lateinit var clientIdentifier: String
    private var lastMessageId = -1
    private var originalMessageIdMap = ConcurrentHashMap<MqttSubscribe, Int>()
    private var reconnectToOriginalMessageIdMap = ConcurrentHashMap<Int, Int>()

    override fun subscribe(
        clientConfig: MqttClientConfig,
        clientComponent: ClientComponent,
        subscribe: MqttSubscribe,
        callback: SubscribedPublishListener,
    ) {
        this.clientComponent = clientComponent
        this.clientIdentifier = clientConfig.identifier
        val nativeClient = clientComponent.clientComponent
        val mqttCallbacks = clientComponent.kMqttCallbacks

        scope.launch {
            while (!hasSession && isActive) {
                delay(100)
            }
            clientSubscriptions.add(subscribe)
            val returnResult = nativeClient.subscribe(
                clientConfig.identifier,
                subscribe.topic,
                subscribe.qos.value,
            )
            val currentMessageId = returnResult.messageID
            lastMessageId = currentMessageId
            originalMessageIdMap[subscribe] = currentMessageId
            mqttCallbacks.registerCallback(subscribe.topic, currentMessageId, callback)

            if (returnResult.code != MOSQUITTO_API_SUCCESS_CODE) {
                callback.onFailure(
                    SubscribeFailException.SystemAPIException(
                        returnResult.code,
                        returnResult.descriptor,
                    ),
                )
            }
        }
    }

    override fun onSubAckReceived(
        messageId: Int,
        isSuccess: Boolean,
    ) {
        scope.launch {
            delay(50)
            var originalSubMessageId = messageId
            reconnectToOriginalMessageIdMap[messageId]?.let {
                if (lastMessageId != -1 && messageId > lastMessageId) {
                    originalSubMessageId = it
                }
            }
            if (this@KMqttSubscriptionHandlerImpl::clientComponent.isInitialized.not()) {
                return@launch
            }
            val mqttCallbacks = clientComponent.kMqttCallbacks
            if (isSuccess) {
                mqttCallbacks.getCallbackByMessageId(
                    originalSubMessageId,
                )?.onSuccess(
                    MqttSubAck(
                        MOSQUITTO_API_SUCCESS_CODE,
                        MOSQUITTO_API_SUCCESS_DESCRIPTOR,
                        originalSubMessageId,
                    ),
                )
            } else {
                mqttCallbacks.getCallbackByMessageId(originalSubMessageId)?.onFailure(
                    SubscribeFailException.BrokerRejectionException(
                        SUBSCRIBE_REJECT_BY_BROKER_ERROR_CODE,
                        SUBSCRIBE_REJECT_BY_BROKER_ERR_DESCRIPTION,
                    ),
                )
            }
        }
    }

    override fun onSessionStartOrResume() {
        if (!hasSession) {
            reSubscribeAll()
        }
        super.onSessionStartOrResume()
    }

    override fun onSessionEnd(cause: Throwable) {
        super.onSessionEnd(cause)
    }

    private fun reSubscribeAll() {
        if (this@KMqttSubscriptionHandlerImpl::clientComponent.isInitialized.not() ||
            this@KMqttSubscriptionHandlerImpl::clientIdentifier.isInitialized.not()
        ) {
            return
        }
        val nativeClient = clientComponent.clientComponent
        val clientIdentifier = clientIdentifier

        scope.launch {
            controlledRunner.cancelPreviousThenRun {
                clientSubscriptions.getAll().forEach { subscription ->
                    val result = nativeClient.subscribe(
                        clientID = clientIdentifier,
                        topic = subscription.topic,
                        qos = subscription.qos.value,
                    )
                    originalMessageIdMap[subscription]?.let { old ->
                        reconnectToOriginalMessageIdMap[result.messageID] = old
                    }
                }
            }
        }
    }
}
