package com.nurshuvo.kmqtt.internal.customerror.subscribe

sealed class SubscribeFailException(
    errorCode: Int,
    errorMessage: String,
) : Exception(errorMessage) {

    class SystemAPIException(
        errorCode: Int,
        message: String,
    ) : SubscribeFailException(errorCode, message)

    class BrokerRejectionException(
        errorCode: Int,
        message: String,
    ) : SubscribeFailException(errorCode, message)
}
