package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

internal interface NativePublishEvent {

    @Keep
    fun onPublishEvent(
        mid: Int,
    )
}
