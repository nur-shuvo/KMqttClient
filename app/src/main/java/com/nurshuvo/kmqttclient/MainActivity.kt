package com.nurshuvo.kmqttclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nurshuvo.kmqttclient.ui.MqttScreenTCP
import com.nurshuvo.kmqttclient.ui.MqttScreenTlsUnauthenticated

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SelectionMode()
        }
    }
}

@Composable
fun SelectionMode() {
    var selectedOption by remember { mutableStateOf("TCP") }
    val options = listOf("TCP", "TLS unauthenticated")

    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
    ) {
        options.forEach { option ->
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { selectedOption = option }
                    )
                    .padding(8.dp)
            ) {
                item {
                    RadioButton(
                        selected = (option == selectedOption),
                        onClick = { selectedOption = option }
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (selectedOption) {
            "TCP" -> MqttScreenTCP()
            "TLS unauthenticated" -> MqttScreenTlsUnauthenticated()
        }
    }
}

