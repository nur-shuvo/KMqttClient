package com.nurshuvo.kmqtt.internal.handler

abstract class MqttSessionAwareHandler {
    protected var hasSession: Boolean = false

    open fun onSessionStartOrResume() {
        hasSession = true
    }

    open fun onSessionEnd(cause: Throwable) {
        hasSession = false
    }
}
