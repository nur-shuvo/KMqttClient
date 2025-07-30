package com.nurshuvo.kmqtt.internal.exceptions.connect

sealed class ConnectionFailException(
    val errorCode: Int,
    errorMessage: String,
) : Exception(errorMessage) {
    class SystemAPIException(
        errorCode: Int,
        message: String,
    ) : ConnectionFailException(errorCode, message)

    class BrokerRejectionException(
        errorCode: Int,
        message: String,
    ) : ConnectionFailException(errorCode, message)

    class TimeOutException(
        errorCode: Int,
        message: String,
    ) : ConnectionFailException(errorCode, message)
}
