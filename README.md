# 🛰️ KMqtt – Kotlin MQTT Client Library (Syntactic sugar of [Mosquitto](https://github.com/eclipse-mosquitto/mosquitto))

[![Maven Central](https://img.shields.io/maven-central/v/io.github.nur-shuvo/KMqttClient.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.nur-shuvo/KMqttClient)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

KMqtt is a low latency, Kotlin-first, coroutine-friendly [MQTT 5.0](https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html) client for **Android**. It simplifies MQTT integration by offering clean, idiomatic APIs on top of the native Mosquitto C library.

## 🚀 Motivation

KMqttClient was created to provide a modern, Kotlin-first, coroutine-friendly MQTT client for developers building IoT and realtime applications. Existing MQTT clients in the JVM ecosystem are often designed with older Java patterns, include heavy abstractions, or introduce unnecessary overhead. This library aims to offer a cleaner, more idiomatic alternative that integrates naturally with Kotlin and Android projects.

A core motivation behind KMqttClient is performance. By building on top of a lightweight native MQTT implementation and exposing only the essential, modern Kotlin APIs, KMqttClient stays fast, efficient, and minimal. Unlike larger MQTT libraries that carry additional layers, legacy features, or broad compatibility code, KMqttClient focuses on being small in size, low in memory footprint, and fast in message handling. This makes it especially suitable for resource-constrained environments such as mobile devices, embedded systems, and IoT clients.

By open-sourcing the library under the MIT license, the aim is to promote collaboration and provide a reliable, community-driven tool that evolves over time. Developers no longer need to reinvent the wheel or manually wrap native MQTT libraries — KMqttClient offers a simple, efficient, and extensible foundation.

Ultimately, KMqttClient exists to make MQTT development in Kotlin easier, faster, and more lightweight, while contributing to an open-source ecosystem where shared improvements benefit everyone.

## 🧬 How It Works

KMqtt is designed with a minimal, idiomatic Kotlin approach to abstract away the complexity of MQTT protocol handling.

- 🏗️ **NDK, CMake and JNI**: Under the hood, it uses Mosquitto C code that runs natively via the NDK, built using CMake. JNI bridges Kotlin with native code, enabling high-performance MQTT operations.
- 🔄 **Coroutines + Flow**: Coroutine and Flow APIs make it easy to handle asynchronous publish/subscribe operations.
- 🧠 **Topic Matching**: Built-in topic matcher ensures you receive only what you're subscribed to, including `+` and `#` wildcards.
- ✨ **Simplified API**: Clean abstractions and optional builder APIs reduce boilerplate and make integration seamless.

## 🚀 Features

- 🧹 Clean, idiomatic Kotlin APIs
- ✅ MQTT 5.0 protocol
- 🔄 Coroutine-based publish/subscribe with `Flow`
- 🔐 Username/password, TLS, and mutual-TLS authentication
- 🎚️ Full QoS 0 / 1 / 2 support
- 📦 Lightweight and modular

## 📋 Requirements

| Requirement | Value |
|-------------|-------|
| Platform    | Android only (ships native `.so` libraries) |
| `minSdk`    | 29 |
| `compileSdk`| 34 |
| JDK         | 21 |
| ABIs        | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |

> **Note:** This is an Android library (`com.android.library`) with a JNI layer. It does not run on plain Kotlin/JVM or Kotlin Multiplatform targets.

## 📦 Installation

Add the Maven Central repository (if not already added):

```kotlin
repositories {
    mavenCentral()
}
```

Then add the dependency in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.nur-shuvo:KMqttClient:1.0.1")
}
```

Finally, declare the internet permission in your app's `AndroidManifest.xml` — the library does not declare it for you:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Imports

All public types live under `com.nurshuvo.kmqtt.internal`:

```kotlin
import com.nurshuvo.kmqtt.internal.MqttClient
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.MqttQos
import com.nurshuvo.kmqtt.internal.controller.subscribe.result.MqttSubscriptionResult
import com.nurshuvo.kmqtt.internal.flowable.MqttSubscribedPublishFlowable
import com.nurshuvo.kmqtt.internal.message.connect.Authentication
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublish
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubscribe
```

## 🛠️ Basic Usage

> `connect`, `publish`, and `unSubscribe` are **suspend** functions — call them from a coroutine
> (for example `lifecycleScope.launch { ... }`). `subscribe` is not suspending; it returns a cold
> `Flow` that starts the subscription when you collect it.

### 1. Create an `MqttClient` instance

```kotlin
val client = MqttClient(
    MqttClientConfig(
        identifier = "client-id",
        serverHost = "broker.hivemq.com",
        serverPort = 1883,
        cleanSession = true
    )
)
```

### 2. Connect to MQTT Broker

```kotlin
lifecycleScope.launch {
    val connectResult = client.connect(
        MqttConnect(
            keepAlive = 60,
            reconnectDelay = 1,
            sendMaximum = 20,
            receiveMaximum = 20,
            authentication = Authentication.NoAuthentication
        )
    )

    connectResult.onSuccess { connAck ->
        Log.d(TAG, "Connected successfully! code=${connAck.code} ${connAck.descriptor}")
    }.onFailure {
        Log.e(TAG, "Connection failed", it)
    }
}
```

### 3. Subscribe to a Topic

```kotlin
val subscribeFlowable = client.subscribe(
    MqttSubscribe(
        topic = "topic/new/shuvo",
        qos = MqttQos.AT_MOST_ONCE
    )
)
```

Then collect incoming messages:

```kotlin
lifecycleScope.launch {
    subscribeFlowable.collect {
        Log.d(TAG, "Received topic=${it.topic}, payload=${it.payload}")
    }
}
```

To observe whether the SUBSCRIBE itself was acknowledged by the broker, attach a single-result
consumer **before** collecting:

```kotlin
subscribeFlowable.doOnSingle { result ->
    when (result) {
        is MqttSubscriptionResult.Success ->
            Log.d(TAG, "SUBACK code=${result.subAck.code} id=${result.subAck.messageID}")
        is MqttSubscriptionResult.Failed ->
            Log.e(TAG, "Subscribe failed", result.exception)
    }
}
```

### 4. Publish a Message

```kotlin
lifecycleScope.launch {
    client.publish(
        MqttPublish(
            topic = "topic/new/shuvo",
            payload = "Hello MQTT!",
            qos = MqttQos.AT_MOST_ONCE,
            retain = false
        )
    ).onSuccess {
        Log.d(TAG, "Published successfully")
    }.onFailure {
        Log.e(TAG, "Publish failed", it)
    }
}
```

### 5. Unsubscribe

```kotlin
lifecycleScope.launch {
    client.unSubscribe(subscribeFlowable)
}
```

## 🔐 Authentication

### Username & password

Credentials are supplied through `MqttClientConfig` (not through `MqttConnect`):

```kotlin
val client = MqttClient(
    MqttClientConfig(
        identifier = "client-id",
        serverHost = "broker.hivemq.com",
        serverPort = 1883,
        cleanSession = true,
        username = "my-username",
        password = "my-password"
    )
)
```

This can be combined with any of the TLS modes below.

### TLS encrypted, unauthenticated

Verifies the broker with a CA certificate; the client presents no certificate of its own.

```kotlin
client.connect(
    MqttConnect(
        keepAlive = 60,
        reconnectDelay = 1,
        sendMaximum = 20,
        receiveMaximum = 20,
        authentication = Authentication.TlsAuthentication(
            certificateAuthorityPath = certPath
        )
    )
)
```

### TLS encrypted, authenticated (mutual TLS)

Additionally presents a client certificate and private key to the broker.

```kotlin
client.connect(
    MqttConnect(
        keepAlive = 60,
        reconnectDelay = 1,
        sendMaximum = 20,
        receiveMaximum = 20,
        authentication = Authentication.TlsAuthentication(
            certificateAuthorityPath = caPath,
            clientCertificatePath = clientCertPath,
            privateKeyPath = privateKeyPath
        )
    )
)
```

All paths are filesystem paths readable by your app — copy bundled assets into
`context.filesDir` (or similar) before connecting.

> The sample app currently demonstrates TCP and TLS-unauthenticated only; mutual TLS is
> supported by the API but not yet covered by a sample screen.

## 🏗️ Builder APIs

Every configuration object also exposes a builder, useful from Java or when values are assembled
conditionally:

```kotlin
val client = MqttClient.builder()
    .setIdentifier("client-id")
    .setServerHost("broker.hivemq.com")
    .setServerPort(1883)
    .setCleanSession(true)
    .build()

val connect = MqttConnect.builder()
    .setKeepALive(60)
    .setReconnectDelay(1)
    .setSendMaximum(20)
    .setReceiveMaximum(20)
    .setAuthentication(Authentication.NoAuthentication)
    .build()

val subscribe = MqttSubscribe.builder()
    .setTopicFilter("topic/new/shuvo")
    .setQos(MqttQos.AT_MOST_ONCE)
    .build()

val publish = MqttPublish.builder()
    .setTopic("topic/new/shuvo")
    .setPayload("Hello MQTT!")
    .setQos(MqttQos.AT_MOST_ONCE)
    .setRetain(false)
    .build()
```

> `MqttClientBuilder` does not expose `username`/`password`; use the `MqttClientConfig`
> constructor when you need credentials.

## 🧪 Example: Full Integration over TCP

```kotlin
private const val TAG = "MainActivity"
private const val CLIENT_ID = "nurshuvo675676"

/**
 * Demonstrates a simple usage of the library with a TCP connection.
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
            identifier = CLIENT_ID,
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
        return client.subscribe(mqttSubscribe)
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
            client.connect(mqttConnect)
                .onSuccess {
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
}
```

A runnable sample app lives in the [`app`](app) module, with separate screens for TCP and
TLS-unauthenticated connections.

## 🧩 API Overview

| Operation      | Call                     | Configuration object | Result |
|----------------|--------------------------|----------------------|--------|
| Connect        | `client.connect(...)`    | `MqttConnect`        | `Result<MqttConnAck>` |
| Publish        | `client.publish(...)`    | `MqttPublish`        | `Result<MqttPublishAck>` |
| Subscribe      | `client.subscribe(...)`  | `MqttSubscribe`      | `MqttSubscribedPublishFlowable` (a `Flow<MqttIncomingPublish>`) |
| Unsubscribe    | `client.unSubscribe(...)`| the flowable from `subscribe` | `Result<MqttUnSubAck>` |

| Concept        | Type |
|----------------|------|
| Authentication | `Authentication.NoAuthentication`, `Authentication.TlsAuthentication` |
| QoS            | `MqttQos.AT_MOST_ONCE` (0), `MqttQos.AT_LEAST_ONCE` (1), `MqttQos.EXACTLY_ONCE` (2) |
| Incoming message | `MqttIncomingPublish(topic, payload, qos, retain)` |
| Subscribe ack  | `MqttSubscriptionResult.Success` / `MqttSubscriptionResult.Failed` via `doOnSingle` |

## 🤝 Contributing

We welcome all contributions — whether it's fixing bugs, improving documentation, adding new features, or writing sample apps. Your ideas and feedback are valuable!

### Building from source

```bash
git clone https://github.com/nur-shuvo/KMqttClient.git
cd KMqttClient
./gradlew build                                      # build everything
./gradlew :mqttclient:mosquitto-mqtt-client-app:build # library only
./gradlew :app:installDebug                          # run the sample app
./gradlew publishToMavenLocal                        # publish locally for testing
```

Building the native layer requires NDK `29.0.13599879` and CMake `3.22.1`.

### Ways to Contribute

- 🛠 Improve code quality or structure
- 🧪 Add tests or sample usage
- 📝 Enhance documentation
- 💡 Suggest new features or improvements
- 🐞 Report or fix bugs

Please open an [issue](https://github.com/nur-shuvo/KMqttClient/issues) to discuss substantial
changes before sending a pull request.

## 📄 License

Released under the [MIT License](LICENSE).

## Built by

Asaduzzaman Nur Shuvo

Email: nurshuvo51@gmail.com
