@file:JvmSynthetic

package com.walletconnect.auth.common.exceptions

import com.walletconnect.android.internal.common.model.Error

internal sealed class PeerError : Error {
    object SignatureVerificationFailed : PeerError() {
        override val message: String = "Signature verification failed"
        override val code: Int = 11004
    }

    object MissingIssuer : PeerError() {
        override val message: String = "Missing issuer"
        override val code: Int = 11005
    }
}