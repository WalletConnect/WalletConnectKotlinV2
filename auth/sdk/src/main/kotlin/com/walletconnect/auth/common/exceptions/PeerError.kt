@file:JvmSynthetic

package com.walletconnect.auth.common.exceptions

import com.walletconnect.android.impl.common.model.type.Error

internal sealed class PeerError : Error {
    object MissingIssuer : PeerError() {
        override val message: String = "Missing issuer"
        override val code: Int = 19001 // todo: specify with docs
    }

    object SignatureVerificationFailed : PeerError() {
        override val message: String = "Signature verification failed"
        override val code: Int = 11004 // todo: specify with docs
    }

}