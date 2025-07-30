package com.nurshuvo.kmqtt.internal.message.unsubscribe

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CrucialClassForJNI
import com.nurshuvo.kmqtt.internal.annotation.CrucialFieldForJNI

@CrucialClassForJNI
@Keep
data class MqttUnSubAck(
    @CrucialFieldForJNI val code: Int,
    @CrucialFieldForJNI val descriptor: String,
    @CrucialFieldForJNI val messageID: Int,
)
