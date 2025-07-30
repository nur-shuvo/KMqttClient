package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

internal interface NativeSubscribeEvent {

    @Keep
    fun onSubscribeEvent(
        mid: Int,
        isSuccess: Boolean,
    )
}
