package com.nurshuvo.kmqtt.internal.handler.publish.incoming

import com.nurshuvo.kmqtt.internal.callbacks.MqttCallbacks
import com.nurshuvo.kmqtt.internal.callbacks.SubscribedPublishListener
import com.nurshuvo.kmqtt.internal.matchtopic.TopicMatch
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.qos.MqttQos
import com.nurshuvo.kmqtt.internal.subscriptions.ClientSubscriptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class MqttIncomingPublishHandlerImpl(
    private val clientSubscriptions: ClientSubscriptions,
    private val mqttCallbacks: MqttCallbacks,
) : MqttIncomingPublishHandler {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageArrived(
        topic: String,
        payload: String,
        qos: MqttQos,
        retain: Boolean,
    ) {
        scope.launch {
            clientSubscriptions.getTopics().onEach { topicFilter ->
                if (TopicMatch.isMatched(topicFilter, topic)) {
                    mqttCallbacks.getCallBacksByTopic(topicFilter)?.forEach {
                        it.values.forEach { callback ->
                            supervisorScope {
                                invokeCallbackConcurrent(
                                    callback,
                                    topicFilter,
                                    payload,
                                    qos,
                                    retain,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CoroutineScope.invokeCallbackConcurrent(
        callback: SubscribedPublishListener,
        topicFilter: String,
        payload: String,
        qos: MqttQos,
        retain: Boolean,
    ) {
        launch {
            callback.onMessageReceived(
                MqttPublish(
                    topicFilter,
                    payload,
                    qos,
                    retain,
                ),
            )
        }
    }
}
