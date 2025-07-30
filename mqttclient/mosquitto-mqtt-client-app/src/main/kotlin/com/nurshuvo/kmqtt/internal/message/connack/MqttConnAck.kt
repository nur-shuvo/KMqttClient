package com.nurshuvo.kmqtt.internal.message.connack

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CrucialClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CrucialFieldForJNI

@CrucialClassForJNI
@Keep
data class MqttConnAck(
    @CrucialFieldForJNI val code: Int,
    @CrucialFieldForJNI val descriptor: String,
)
