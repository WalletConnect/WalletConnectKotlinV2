package com.walletconnect.walletconnectv2.core.exceptions.peer

internal sealed class PeerError {
    abstract val message: String
    abstract val code: Int

    //Validation errors

    data class InvalidMethod(val reason: String) : PeerError() {
        override val message: String = "Invalid session request: $reason"
        override val code: Int = 1001
    }

    data class InvalidEvent(val reason: String) : PeerError() {
        override val message: String = "Invalid event request: $reason"
        override val code: Int = 1002
    }

    data class InvalidUpdateRequest(val reason: String) : PeerError() {
        override val message: String = "Invalid update namespace request: $reason"
        override val code: Int = 1003
    }

    data class InvalidExtendRequest(val reason: String) : PeerError() {
        override val message = "Invalid session extend request: $reason"
        override val code: Int = 1004
    }

    //Authorization errors

    data class UnauthorizedMethod(val reason: String) : PeerError() {
        override val message: String = "Unauthorized session request: $reason"
        override val code: Int = 3001
    }

    data class UnauthorizedEvent(val reason: String) : PeerError() {
        override val message: String = "Unauthorized event request: $reason"
        override val code: Int = 3002
    }

    data class UnauthorizedUpdateRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized update $sequence namespace request"
        override val code: Int = 3003
    }

    data class UnauthorizedExtendRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence extend request"
        override val code: Int = 3004
    }

    data class UnauthorizedEventEmit(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence event emit"
        override val code: Int = 3006
    }

    //Rejected errors

    data class UserRejected(override val message: String) : PeerError() {
        override val code: Int = 5000
    }

    data class UserRejectedChains(override val message: String) : PeerError() {
        override val code: Int = 5001
    }

    data class UserRejectedMethods(override val message: String) : PeerError() {
        override val code: Int = 5002
    }

    data class UserRejectedEvents(override val message: String) : PeerError() {
        override val code: Int = 5003
    }

    //Uncategorized errors

    data class NoMatchingTopic(val sequence: String, val topic: String) : PeerError() {
        override val message: String = "No matching $sequence with topic: $topic"
        override val code: Int = 1301
    }

    data class SessionSettlementFailed(val reason: String) : PeerError() {
        override val message: String = "Invalid Session Settle Request: $reason"
        override val code: Int = 7000
    }

    data class Error(val reason: String, val errorCode: Int) : PeerError() {
        override val message: String = reason
        override val code: Int = errorCode
    }

    //Namespaces validation

    data class UnsupportedNamespaceKey(override val message: String) : PeerError() {
        override val code: Int = 5104
    }

    data class UnsupportedChains(override val message: String) : PeerError() {
        override val code: Int = 5100
    }
}