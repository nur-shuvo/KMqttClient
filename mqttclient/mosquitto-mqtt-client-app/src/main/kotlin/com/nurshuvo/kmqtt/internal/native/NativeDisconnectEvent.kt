package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

internal interface NativeDisconnectEvent {

    @Keep
    fun onDisconnectEvent(
        reasonCode: Int,
        reasonDescriptor: String,
    )
}
