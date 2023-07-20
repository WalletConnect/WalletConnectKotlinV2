package com.walletconnect.push.common

import com.walletconnect.android.internal.common.model.type.Error

/**
 * Documentation: https://github.com/WalletConnect/walletconnect-docs/blob/main/docs/specs/clients/push/error-codes.md
 */
sealed class PeerError : Error {

    sealed class Invalid: PeerError() {

        data class Proposal(val reason: String) : Invalid() {
            override val message: String = "Invalid session request: $reason"
            override val code: Int = 1000
        }
    }

    sealed class Rejected: PeerError() {

        data class UserRejected(override val message: String) : Rejected() {
            override val code: Int = 5000
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