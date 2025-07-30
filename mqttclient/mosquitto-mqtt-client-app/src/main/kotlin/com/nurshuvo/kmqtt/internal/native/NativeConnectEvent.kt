package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

internal interface NativeConnectEvent {

    @Keep
    fun onConnectEvent(
        reasonCode: Int,
        reasonDescriptor: String,
    )
}
