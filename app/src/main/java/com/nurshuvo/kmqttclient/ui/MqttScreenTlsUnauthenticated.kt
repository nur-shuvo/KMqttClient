package com.nurshuvo.kmqttclient.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.nurshuvo.kmqtt.internal.MqttClient
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.MqttQos
import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.connect.Authentication
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Shuvo on 09/01/2025.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttScreenTlsUnauthenticated() {
    val scope = rememberCoroutineScope()
    var isConnected by remember { mutableStateOf(false) }
    var messageLog by remember { mutableStateOf(listOf<String>()) }
    var publishMessage by remember { mutableStateOf("Hello MQTT!") }

    var serverHost by remember { mutableStateOf("test.mosquitto.org") }
    var serverPort by remember { mutableStateOf("8883") }

    var client: MqttClient? by remember { mutableStateOf(null) }
    lateinit var subscribeFlowable: MqttSubscribedPublishFlowable

    fun log(message: String) {
        messageLog = messageLog + message
    }

    val context = LocalContext.current

    val crtFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val name = uri.getFileName(context)
                if (name != null && name.endsWith(".crt", ignoreCase = true)) {
                    saveUriToAppFiles(context, uri, name)
                } else {
                    println("❌ Not a .crt file")
                }
            }
        }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("KMqtt Sample (TLS unauthenticated)") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = {
                    crtFilePickerLauncher.launch(
                        arrayOf(
                            "application/x-x509-ca-cert",
                            "*/*"
                        )
                    )
                }) {
                    Text("Upload certificate authority file")
                }
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
                val context = LocalContext.current
                val certFile = File(context.filesDir, "mosquitto.org.crt")
                val certPath = certFile.absolutePath
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
                                        serverPort = serverPort.toIntOrNull() ?: 8883,
                                        cleanSession = true,
                                        authentication = Authentication.TlsAuthentication(
                                            certificateAuthorityPath = certPath,
                                        )
                                    )
                                )
                                client?.connect(
                                    MqttConnect(
                                        keepAlive = 60,
                                        reconnectDelay = 1,
                                        sendMaximum = 20,
                                        receiveMaximum = 20,
                                        authentication = Authentication.TlsAuthentication(
                                            certificateAuthorityPath = certPath,
                                        )
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
            }

            items(messageLog) { msg ->
                Text(text = msg, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun Uri.getFileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
    }
}

private fun saveUriToAppFiles(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("Cannot open input stream")
    val outFile = File(context.filesDir, fileName)

    FileOutputStream(outFile).use { output ->
        inputStream.copyTo(output)
    }
    inputStream.close()

    return outFile
}