package com.nurshuvo.kmqtt.internal

enum class MqttQos(
    val value: Int,
) {
    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2),
    ;

    companion object {
        val DEFAULT: MqttQos = AT_MOST_ONCE
        fun valueOf(
            value: Int,
        ) =
            entries.firstOrNull {
                it.value == value
            } ?: AT_LEAST_ONCE
    }
}