package com.nurshuvo.kmqtt.internal.message.subscribe

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CarefulFieldForJNI

@CarefulRenameClassForJNI
@Keep
data class MqttSubAck(
    @CarefulFieldForJNI val code: Int,
    @CarefulFieldForJNI val descriptor: String,
    @CarefulFieldForJNI val messageID: Int,
)
