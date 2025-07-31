package com.nurshuvo.kmqtt.internal.message.connack

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CarefulFieldForJNI

@CarefulRenameClassForJNI
@Keep
data class MqttConnAck(
    @CarefulFieldForJNI val code: Int,
    @CarefulFieldForJNI val descriptor: String,
)
