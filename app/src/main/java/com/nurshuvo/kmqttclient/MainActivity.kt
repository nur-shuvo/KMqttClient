package com.nurshuvo.kmqttclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val client = createClient()
    private lateinit var subscribeFlowable: MqttSubscribedPublishFlowable

    private fun createClient(): MqttClient = MqttClient(
        MqttClientConfig(
            identifier = SERVER_HOST,
            serverHost = "broker.hivemq.com",
            serverPort = 1883,
            cleanSession = true
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MqttScreen()
        }
    }

    @Composable
    fun MqttScreen() {
        val scope = rememberCoroutineScope()
        var isConnected by remember { mutableStateOf(false) }
        var messageLog by remember { mutableStateOf(listOf<String>()) }
        var publishMessage by remember { mutableStateOf("Hello MQTT!") }

        fun log(message: String) {
            messageLog = messageLog + message
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("KMqtt Sample") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = {
                        scope.launch {
                            val connectResult = client.connect(
                                MqttConnect(
                                    keepAlive = 60,
                                    reconnectDelay = 1,
                                    sendMaximum = 20,
                                    receiveMaximum = 20,
                                    authentication = Authentication.NoAuthentication
                                )
                            )
                            connectResult.onSuccess {
                                isConnected = true
                                log("✅ Connected to broker")
                            }.onFailure {
                                log("❌ Connection failed: ${it.message}")
                            }
                        }
                    }) {
                        Text("Connect")
                    }

                    Button(
                        onClick = {
                            if (!isConnected) {
                                log("⚠️ Not connected to broker")
                                return@Button
                            }
                            subscribeFlowable = client.subscribe(
                                MqttSubscribe(
                                    topic = "topic/new/shuvo",
                                    qos = MqttQos.AT_MOST_ONCE
                                )
                            )
                            scope.launch {
                                subscribeFlowable.collect {
                                    val received = "📥 Received : ${it.topic}: ${it.payload}"
                                    log(received)
                                }
                            }
                            log("🔔 Subscribed to topic topic/new/shuvo")
                        }
                    ) {
                        Text("Subscribe")
                    }

                    OutlinedTextField(
                        value = publishMessage,
                        onValueChange = { publishMessage = it },
                        label = { Text("Message to Publish") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (!isConnected) {
                                log("⚠️ Not connected to broker")
                                return@Button
                            }
                            scope.launch {
                                val result = client.publish(
                                    MqttPublish(
                                        topic = "topic/new/shuvo",
                                        payload = publishMessage,
                                        qos = MqttQos.AT_MOST_ONCE,
                                        retain = false
                                    )
                                )
                                result.onSuccess {
                                    log("📤 Published message: \"$publishMessage\"")
                                }.onFailure {
                                    log("❌ Publish failed: ${it.message}")
                                }
                            }
                        }
                    ) {
                        Text("Publish")
                    }

                    Divider()

                    Text("📨 Messages Received:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp)
                    ) {
                        items(messageLog) { msg ->
                            Text(text = msg, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        )
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SERVER_HOST = "nurshuvo675676"
    }
}
