package com.nurshuvo.kmqtt.internal.message.subscribe

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CrucialClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CrucialFieldForJNI

@CrucialClassForJNI
@Keep
data class MqttSubAck(
    @CrucialFieldForJNI val code: Int,
    @CrucialFieldForJNI val descriptor: String,
    @CrucialFieldForJNI val messageID: Int,
)
