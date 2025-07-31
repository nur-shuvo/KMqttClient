package com.nurshuvo.kmqttclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.nurshuvo.kmqtt.internal.MqttClient
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.connect.Authentication
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import com.nurshuvo.kmqtt.internal.qos.MqttQos
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private const val TAG = "MainActivity"
private const val SERVER_HOST = "nurshuvo675676"

/**
 * Demonstrates a simple usage of the library with TCP connection.
 */
class MainActivity : ComponentActivity() {

    private val client = createClient()
    private val subscribeFlowable =
        createSubscribeFlowable(
            topic = "topic/new/shuvo",
            qos = MqttQos.AT_MOST_ONCE
        )

    private fun createClient(): MqttClient = MqttClient(
        MqttClientConfig(
            identifier = SERVER_HOST,
            serverHost = "broker.hivemq.com",
            serverPort = 1883,
            cleanSession = true
        )
    )

    private fun createSubscribeFlowable(
        topic: String,
        qos: MqttQos
    ): MqttSubscribedPublishFlowable {
        val mqttSubscribe = MqttSubscribe(topic, qos)
        return client.subscribe(
            mqttSubscribe
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectToMqttBroker()
        publishMessageWithDelay()
    }

    private fun connectToMqttBroker() {
        lifecycleScope.launch {
            val mqttConnect = MqttConnect(
                keepAlive = 60,
                reconnectDelay = 1,
                sendMaximum = 20,
                receiveMaximum = 20,
                authentication = Authentication.NoAuthentication
            )
            val connectResult = client.connect(mqttConnect)
            connectResult.onSuccess {
                Log.d(TAG, "Connect: result success $it")
                observeIncomingMessages()
            }.onFailure {
                Log.d(TAG, "Connect: result failure $it")
            }
        }
    }

    private fun publishMessageWithDelay() {
        lifecycleScope.launch {
            while (isActive) {
                delay(30.seconds)
                val mqttPublish = MqttPublish(
                    topic = "topic/new/shuvo",
                    payload = "payload",
                    qos = MqttQos.AT_MOST_ONCE,
                    retain = false
                )
                client.publish(mqttPublish)
                    .onSuccess {
                        Log.d(TAG, "publish: result success $it")
                    }.onFailure {
                        Log.d(TAG, "publish: result failure $it")
                    }
            }
        }
    }

    private fun observeIncomingMessages() {
        lifecycleScope.launch {
            subscribeFlowable.collect {
                Log.d(TAG, "received message topic: ${it.topic} payload: ${it.payload}")
            }
        }
    }

    private fun unSubscribe() {
        lifecycleScope.launch {
            client.unSubscribe(subscribeFlowable)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
