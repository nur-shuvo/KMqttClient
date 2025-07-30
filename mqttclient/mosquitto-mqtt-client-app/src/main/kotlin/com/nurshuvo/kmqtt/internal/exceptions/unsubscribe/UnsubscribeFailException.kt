package com.nurshuvo.kmqtt.internal.exceptions.unsubscribe

sealed class UnsubscribeFailException(
    errorCode: Int,
    errorMessage: String,
) : Exception(errorMessage) {

    class SystemAPIException(
        errorCode: Int,
        message: String,
    ) : UnsubscribeFailException(errorCode, message)

    class TimeOutException(
        errorCode: Int,
        message: String,
    ) : UnsubscribeFailException(errorCode, message)
}
