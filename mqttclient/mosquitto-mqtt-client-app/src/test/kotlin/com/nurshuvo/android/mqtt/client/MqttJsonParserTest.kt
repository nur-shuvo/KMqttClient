package com.nurshuvo.android.mqtt.client

import com.nurshuvo.kmqtt.common.model.adapter.CommonModelSerializeAdapter
import com.nurshuvo.kmqtt.nurshuvo.kmqttclient.serialize.MqttSerializeAdapterImpl
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Moshi.Builder
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MqttJsonParserTest {

    private lateinit var moshi: Moshi
    private lateinit var mqttSerializeAdapter: MqttSerializeAdapterImpl

    @JsonClass(generateAdapter = true)
    data class Days(
        val friday: Boolean,
        val monday: String,
        val saturday: Int,
    )

    @Before
    fun setUp() {
        moshi = Builder().build()
        mqttSerializeAdapter = MqttSerializeAdapterImpl(CommonModelSerializeAdapter.moshi)
    }

    @Test
    fun dataClassTest() {
        val day = Days(true, "friday", 1)
        val toJson = mqttSerializeAdapter.toJson(day)
        val fromJson = mqttSerializeAdapter.fromJson<Days>(toJson)
        assertEquals(day, fromJson)
    }

    @Test
    fun dataListClassTest() {
        val day = listOf(
            Days(true, "friday", 1),
            Days(false, "monday", 2),
        )
        val toJson = mqttSerializeAdapter.toJson(day)
        val fromJson = mqttSerializeAdapter.fromJson<List<Days>>(toJson)
        assertEquals(day, fromJson)
    }

    @Test
    fun dataListToStringClassTest() {
        val day = listOf(
            Days(true, "friday", 1),
            Days(false, "monday", 2),
        )
        val toJson = mqttSerializeAdapter.toJson(day)
        val fromJson = mqttSerializeAdapter.fromJson<String>(toJson)
        assertEquals(toJson, fromJson)
    }
}
