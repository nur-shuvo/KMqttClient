package com.nurshuvo.kmqtt.internal.message.connect

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI

@Keep
@CarefulRenameClassForJNI
sealed interface Authentication {
    @Keep
    data class TlsAuthentication(
        val certificateAuthorityPath: String,
        val clientCertificatePath: String? = null,
        val privateKeyPath: String? = null,
    ) : Authentication

    @Keep
    data object NoAuthentication : Authentication
}
