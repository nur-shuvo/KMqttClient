package com.nurshuvo.kmqtt.internal.message.publish.outgoing

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CarefulFieldForJNI

@CarefulRenameClassForJNI
@Keep
data class MqttPublishAck(
    @CarefulFieldForJNI val code: Int,
    @CarefulFieldForJNI val descriptor: String,
    @CarefulFieldForJNI val messageID: Int,
)
