package com.walletconnect.push.common

import com.walletconnect.android.internal.common.model.Error

/**
 * Documentation: https://github.com/WalletConnect/walletconnect-specs/blob/main/sign/error-codes.md
 */
sealed class PeerError : Error {

    sealed class EIP1193 : PeerError() {

        data class UserRejectedRequest(override val message: String) : EIP1193() {
            override val code: Int = 1000
        }
    }
}