package com.nurshuvo.kmqttclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.nurshuvo.kmqtt.internal.MqttClient
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.message.connect.MqttConnect
import com.nurshuvo.kmqttclient.ui.theme.KMqttClientTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val client = MqttClient(
                MqttClientConfig(
                    identifier = "androidClient5454545455123",
                    serverHost = "test.mosquitto.org",
                )
            )
            val connect = MqttConnect.builder().build()
            val isSuccess = client.connect(connect)
            Log.d("MainActivity", "onCreate: isSuccess $isSuccess")
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            val client = MqttClient(
                MqttClientConfig(
                    identifier = "androidClient5454545455123",
                    serverHost = "test.mosquitto.org",
                    serverPort = 1883,
                )
            )
            val connect = MqttConnect.builder().build()
            val isSuccess = client.connect(connect)
            Log.d("MainActivity", "onCreate: isSuccess $isSuccess")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KMqttClientTheme {
        Greeting("Android")
    }
}