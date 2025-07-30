package com.nurshuvo.kmqtt.internal.exceptions.publish

sealed class PublishFailException(
    errorCode: Int,
    errorMessage: String,
) : Exception(errorMessage) {

    class SystemAPIException(
        errorCode: Int,
        message: String,
    ) : PublishFailException(errorCode, message)

    class TimeOutException(
        errorCode: Int,
        message: String,
    ) : PublishFailException(errorCode, message)
}
