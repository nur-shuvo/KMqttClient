package com.nurshuvo.kmqtt.internal.flowable

import com.nurshuvo.kmqtt.internal.ClientComponent
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.callbacks.SubscribedPublishListener
import com.nurshuvo.kmqtt.internal.exceptions.subscribe.SubscribeFailException
import com.nurshuvo.kmqtt.internal.handler.subscribe.result.MqttSubscriptionResult
import com.nurshuvo.kmqtt.internal.handler.subscribe.result.MqttSubscriptionResult.Failed
import com.nurshuvo.kmqtt.internal.handler.subscribe.result.MqttSubscriptionResult.Success
import com.nurshuvo.kmqtt.internal.message.publish.incoming.MqttIncomingPublish
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class MqttSubscribedPublishFlowable<T>(
    val type: Class<T>,
    val clientConfig: MqttClientConfig,
    val clientComponent: ClientComponent,
    val subscribe: MqttSubscribe,
) : FlowableWithSingle<MqttIncomingPublish<T>, MqttSubscriptionResult>() {

    override suspend fun produceFlow(
        resultChannel: Channel<MqttIncomingPublish<T>>,
    ) {
        val subscriptionHandler = clientComponent.subscriptionHandler
        callbackFlow {
            val callback = getSubscribedPublishListener()
            subscriptionHandler.subscribe(
                clientConfig,
                clientComponent,
                subscribe,
                callback,
            )
            awaitClose {
                clientComponent.mqttCallbacks.unRegisterCallback(
                    subscribe.topic,
                    callback,
                )
            }
        }.collect { message ->
            runCatching {
                resultChannel.send(message)
            }.onFailure {
                //logcat(ERROR) { "Current coroutine of the chanel is cancelled ${it.message}" }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun ProducerScope<MqttIncomingPublish<T>>.getSubscribedPublishListener() =
        object : SubscribedPublishListener {

            override fun onSuccess(
                subAck: MqttSubAck,
            ) {
                singleConsumer?.invoke(Success(subAck))
            }

            override fun onFailure(
                exception: SubscribeFailException,
            ) {
                singleConsumer?.invoke(Failed(exception))
            }

            override fun onMessageReceived(
                mqttPublish: MqttPublish,
            ) {
                if (!isClosedForSend) {
                    runCatching {
//                        trySend(
//                            MqttIncomingPublish(
//                                topic = mqttPublish.topic,
//                                payload = MqttSerializer.adapter.fromJson(
//                                    mqttPublish.payload,
//                                    type,
//                                ),
//                                qos = mqttPublish.qos,
//                                retain = mqttPublish.retain,
//                            ),
//                        )
                    }.onFailure {
                        //logcat(ERROR) { "Error in serialization, cause: ${it.message}" }
                    }
                }
            }
        }
}
