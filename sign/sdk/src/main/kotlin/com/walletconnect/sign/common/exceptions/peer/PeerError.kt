package com.walletconnect.sign.common.exceptions.peer

import com.walletconnect.android.impl.common.model.type.Error
import com.walletconnect.sign.common.exceptions.DISCONNECT_MESSAGE

/**
 * Documentation: https://github.com/WalletConnect/walletconnect-specs/blob/main/sign/error-codes.md
 */
internal sealed class PeerError : Error {

    sealed class Invalid: PeerError() {

        data class Method(val reason: String) : Invalid() {
            override val message: String = "Invalid session request: $reason"
            override val code: Int = 1001
        }

        data class Event(val reason: String) : Invalid() {
            override val message: String = "Invalid event request: $reason"
            override val code: Int = 1002
        }

        data class UpdateRequest(val reason: String) : Invalid() {
            override val message: String = "Invalid update namespace request: $reason"
            override val code: Int = 1003
        }

        data class ExtendRequest(val reason: String) : Invalid() {
            override val message = "Invalid session extend request: $reason"
            override val code: Int = 1004
        }

        data class SessionSettleRequest(override val message: String) : Invalid() {
            override val code: Int = 1005
        }
    }

    sealed class Unauthorized: PeerError() {

        data class Method(val reason: String) : Unauthorized() {
            override val message: String = "Unauthorized session request: $reason"
            override val code: Int = 3001
        }

        data class Event(val reason: String) : Unauthorized() {
            override val message: String = "Unauthorized event request: $reason"
            override val code: Int = 3002
        }

        data class UpdateRequest(val sequence: String) : Unauthorized() {
            override val message: String = "Unauthorized update $sequence namespace request"
            override val code: Int = 3003
        }

        data class ExtendRequest(val sequence: String) : Unauthorized() {
            override val message: String = "Unauthorized $sequence extend request"
            override val code: Int = 3004
        }

        data class Chain(override val message: String) : Unauthorized() {
            override val code: Int = 3005
        }
    }

    sealed class EIP1193: PeerError() {

        data class UserRejectedRequest(override val message: String) : EIP1193() {
            override val code: Int = 4001
        }
    }

    sealed class CAIP25: PeerError() {

        data class UserRejected(override val message: String) : CAIP25() {
            override val code: Int = 5000
        }

        data class UserRejectedChains(override val message: String) : CAIP25() {
            override val code: Int = 5001
        }

        data class UserRejectedMethods(override val message: String) : CAIP25() {
            override val code: Int = 5002
        }

        data class UserRejectedEvents(override val message: String) : CAIP25() {
            override val code: Int = 5003
        }

        data class UnsupportedChains(override val message: String) : CAIP25() {
            override val code: Int = 5100
        }

        data class UnsupportedMethods(override val message: String) : CAIP25() {
            override val code: Int = 5101
        }

        data class UnsupportedEvents(override val message: String) : CAIP25() {
            override val code: Int = 5102
        }

        data class UnsupportedAccounts(override val message: String) : CAIP25() {
            override val code: Int = 5103
        }

        data class UnsupportedNamespaceKey(override val message: String) : CAIP25() {
            override val code: Int = 5104
        }
    }

    sealed class Reason: PeerError() {

        object UserDisconnected : Reason() {
            override val message: String = DISCONNECT_MESSAGE
            override val code: Int = 6000
        }
    }

    sealed class Failure: PeerError() {

        data class SessionSettlementFailed(val reason: String) : Failure() {
            override val message: String = "Invalid Session Settle Request: $reason"
            override val code: Int = 7000
        }

        data class NoSessionForTopic(override val message: String) : Failure() {
            override val code: Int = 7001
        }
    }

    sealed class Uncategorized: PeerError() {

        data class NoMatchingTopic(val sequence: String, val topic: String) : PeerError() {
            override val message: String = "No matching $sequence with topic: $topic"
            override val code: Int = 1301
        }
    }
}