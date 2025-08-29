package com.nurshuvo.kmqttclient

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.nurshuvo.kmqtt.internal.MqttQos
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MqttScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttScreen() {
    val scope = rememberCoroutineScope()
    var isConnected by remember { mutableStateOf(false) }
    var messageLog by remember { mutableStateOf(listOf<String>()) }
    var publishMessage by remember { mutableStateOf("Hello MQTT!") }

    var serverHost by remember { mutableStateOf("broker.hivemq.com") }
    var serverPort by remember { mutableStateOf("1883") }

    var client: MqttClient? by remember { mutableStateOf(null) }
    lateinit var subscribeFlowable: MqttSubscribedPublishFlowable

    fun log(message: String) {
        messageLog = messageLog + message
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("KMqtt Sample (TCP)") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = serverHost,
                onValueChange = { serverHost = it },
                label = { Text("MQTT Host") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("MQTT Port") },
                modifier = Modifier.fillMaxWidth()
            )

            // Connect row with button + status text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = {
                    scope.launch {
                        runCatching {
                            client = MqttClient(
                                MqttClientConfig(
                                    identifier = "client_${System.currentTimeMillis()}",
                                    serverHost = serverHost,
                                    serverPort = serverPort.toIntOrNull() ?: 1883,
                                    cleanSession = true
                                )
                            )
                            client?.connect(
                                MqttConnect(
                                    keepAlive = 60,
                                    reconnectDelay = 1,
                                    sendMaximum = 20,
                                    receiveMaximum = 20,
                                    authentication = Authentication.NoAuthentication
                                )
                            )
                        }.onSuccess {
                            isConnected = true
                            log("✅ Connected to $serverHost:$serverPort")
                        }.onFailure {
                            isConnected = false
                            log("❌ Connection failed: ${it.message}")
                        }
                    }
                }) {
                    Text("Connect")
                }

                Text(
                    text = if (isConnected) "✅ Connected" else "❌ Not connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConnected) Color(0xFF2E7D32) else Color.Red,
                    modifier = Modifier.alignByBaseline()
                )
            }

            var topic by remember { mutableStateOf("topic/new/shuvo") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic") },
                    modifier = Modifier.weight(1f) // take remaining space
                )

                Button(
                    onClick = {
                        if (!isConnected || client == null) {
                            log("⚠️ Not connected to broker")
                            return@Button
                        }
                        subscribeFlowable = client!!.subscribe(
                            MqttSubscribe(
                                topic = topic,
                                qos = MqttQos.AT_MOST_ONCE
                            )
                        )
                        scope.launch {
                            subscribeFlowable.collect {
                                val received = "📥 Received : ${it.topic}: ${it.payload}"
                                log(received)
                            }
                        }
                        log("🔔 Subscribed to topic $topic")
                    },
                    modifier = Modifier.alignByBaseline()
                ) {
                    Text("Subscribe")
                }
            }

            OutlinedTextField(
                value = publishMessage,
                onValueChange = { publishMessage = it },
                label = { Text("Message to Publish") },
                modifier = Modifier.fillMaxWidth()
            )

            var publishTopic by remember { mutableStateOf("topic/new/shuvo") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = publishTopic,
                    onValueChange = { publishTopic = it },
                    label = { Text("Publish Topic") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (!isConnected || client == null) {
                            log("⚠️ Not connected to broker")
                            return@Button
                        }
                        scope.launch {
                            client?.publish(
                                MqttPublish(
                                    topic = publishTopic,
                                    payload = publishMessage,
                                    qos = MqttQos.AT_MOST_ONCE,
                                    retain = false
                                )
                            )?.onSuccess {
                                log("📤 Published message to $publishTopic: \"$publishMessage\"")
                            }?.onFailure {
                                log("❌ Publish failed: ${it.message}")
                            }
                        }
                    },
                    modifier = Modifier.alignByBaseline()
                ) {
                    Text("Publish")
                }
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
}
