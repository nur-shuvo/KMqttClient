package com.nurshuvo.kmqtt.internal.message.unsubscribe

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CarefulFieldForJNI

@CarefulRenameClassForJNI
@Keep
data class MqttUnSubAck(
    @CarefulFieldForJNI val code: Int,
    @CarefulFieldForJNI val descriptor: String,
    @CarefulFieldForJNI val messageID: Int,
)
