package com.nurshuvo.kmqtt.internal.message.connect

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CrucialClassForJNI

@Keep
@CrucialClassForJNI
sealed interface Authentication {
    @Keep
    data class TlsAuthentication(
        val certificateAuthorityPath: String,
        val clientCertificatePath: String,
        val privateKeyPath: String,
    ) : Authentication

    @Keep
    data object NoAuthentication : Authentication
}
