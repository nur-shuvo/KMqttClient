package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep

interface NativeMessageEvent {

    @Keep
    fun onMessageEvent(
        mid: Int,
        topic: String,
        len: Int,
        payload: String,
        qos: Int,
        retain: Boolean,
    )
}
