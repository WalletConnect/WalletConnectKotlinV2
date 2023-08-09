@file:JvmSynthetic

package com.walletconnect.notify.common

import com.walletconnect.android.internal.common.model.type.Error

// Documentation: https://github.com/WalletConnect/walletconnect-docs/blob/main/docs/specs/clients/notify/error-codes.md
internal sealed class PeerError : Error {

    sealed class User: PeerError() {
        data class Rejected(override val message: String) : User() {
            override val code: Int = 5000
        }

        data class Unsubscribed(override val message: String) : User() {
            override val code: Int = 6000
        }

        data class HasExistingSubscription(override val message: String) : User() {
            override val code: Int = 6001
        }
    }

    sealed class Failure: PeerError() {

        object ApprovalFailed: Failure() {
            override val message: String = "Approval Failed"
            override val code: Int = 7002
        }

        object RejectionFailed: Failure() {
            override val message: String = "Rejection Failed"
            override val code: Int = 7003
        }
    }
}