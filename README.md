# 🛰️ KMqtt – Kotlin MQTT Client Library

KMqtt is a Kotlin-first, coroutine-friendly MQTT client built on top of [mqtt 5.0](https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html) and also supports mqtt 3.0. It simplifies MQTT integration in Android and Kotlin applications by offering clean, idiomatic APIs.

## 🧬 How It Works

KMqtt is designed with a minimal, idiomatic Kotlin approach to abstract away the complexity of MQTT protocol handling.

- 🏗️ **NDK & CMake**: Under the hood, it used Mosutitto C code that runs native code via the NDK, built using CMake, for high-performance MQTT operations.
- 🔄 **Coroutines + Flow**: Coroutine and Flow APIs make it easy to handle asynchronous publish/subscribe operations.
- 🧠 **Topic Matching**: Built-in topic matcher ensures you receive only what you're subscribed to.
- ✨ **Simplified API**: Cleaner abstractions and DSL-style configuration reduce boilerplate and make integration seamless.


## 🚀 Features

- 🧹 Clean, idiomatic Kotlin APIs
- ✅ Supports MQTT 5.0 and 3.0
- 🔄 Coroutine-based publish/subscribe
- 🔐 Pluggable authentication support
- 📦 Lightweight and modular

## 📦 Installation

Coming soon to **Maven Central** / **JitPack**.

For now, include it as a local module:

## 🛠️ Basic Usage

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
    Log.d(TAG, "Connected successfully!")
}.onFailure {
    Log.e(TAG, "Connection failed", it)
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

### 4. Publish a Message

```kotlin
val result = client.publish(
    MqttPublish(
        topic = "topic/new/shuvo",
        payload = "Hello MQTT!",
        qos = MqttQos.AT_MOST_ONCE,
        retain = false
    )
)

result.onSuccess {
    Log.d(TAG, "Published successfully")
}.onFailure {
    Log.e(TAG, "Publish failed", it)
}
```

### 5. Unsubscribe

```kotlin
client.unSubscribe(subscribeFlowable)
```

## 🧪 Example: Full Integration ie. in `MainActivity`

```kotlin
class MainActivity : ComponentActivity() {

    private val client = MqttClient(...)
    private val subscribeFlowable = client.subscribe(...)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectToBroker()
        startPublishing()
    }

    private fun connectToBroker() {
        lifecycleScope.launch {
            client.connect(...).onSuccess {
                observeMessages()
            }
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            subscribeFlowable.collect {
                Log.d(TAG, "Incoming: ${it.payload}")
            }
        }
    }

    private fun startPublishing() {
        lifecycleScope.launch {
            while (isActive) {
                delay(30.seconds)
                client.publish(...).onSuccess { ... }
            }
        }
    }
}
```

## 🧩 API Overview

| Feature        | Object configuration          |
|----------------|----------------------|
| Connect        | `MqttConnect`        |
| Publish        | `MqttPublish`        |
| Subscribe      | `MqttSubscribe`      |
| Unsubscribe    | `client.unSubscribe()` |
| Authentication | `Authentication.NoAuthentication`, `Authentication.TlsAuthentication` |
| QoS Support    | `MqttQos` (0, 1, 2)   |

## 🤝 Contributing

We welcome all contributions — whether it's fixing bugs, improving documentation, adding new features, or writing sample apps. Your ideas and feedback are valuable!

### Ways to Contribute

- 🛠 Improve code quality or structure
- 🧪 Add tests or sample usage
- 📝 Enhance documentation
- 💡 Suggest new features or improvements
- 🐞 Report or fix bugs
  
## Built by
Asaduzzaman Nur Shuvo

Email: nurshuvo51@gmail.com
