package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

internal interface NativeUnsubscribeEvent {

    @Keep
    fun onUnsubscribeEvent(
        mid: Int,
    )
}
