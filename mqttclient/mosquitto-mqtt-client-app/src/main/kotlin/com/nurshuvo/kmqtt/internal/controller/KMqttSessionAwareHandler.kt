package com.nurshuvo.kmqtt.internal.controller

abstract class KMqttSessionAwareHandler {
    protected var hasSession: Boolean = false

    open fun onSessionStartOrResume() {
        hasSession = true
    }

    open fun onSessionEnd(cause: Throwable) {
        hasSession = false
    }
}
