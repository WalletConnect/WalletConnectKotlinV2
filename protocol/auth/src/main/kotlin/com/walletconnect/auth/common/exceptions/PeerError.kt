@file:JvmSynthetic

package com.walletconnect.auth.common.exceptions

import com.walletconnect.android.internal.common.model.type.Error

internal sealed class PeerError : Error {
    object SignatureVerificationFailed : PeerError() {
        override val message: String = "Signature verification failed"
        override val code: Int = 11004
    }
}