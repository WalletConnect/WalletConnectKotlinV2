package com.walletconnect.walletconnectv2.core.exceptions.peer

sealed class PeerError {
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

    data class InvalidSessionProposeRequest(val topic: String, val errorMessage: String) : PeerError() {
        override val message: String = "Invalid Session Proposal on topic: $topic. Error Message: $errorMessage"
        override val code: Int = 1007
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

    class UserRejected : PeerError() {
        override val message: String = "User Rejected"
        override val code: Int = 5000
    }

    class UserRejectedChains : PeerError() {
        override val message: String = "User Rejected Chains"
        override val code: Int = 5001
    }

    class UserRejectedMethods : PeerError() {
        override val message: String = "User Rejected Methods"
        override val code: Int = 5002
    }

    class UserRejectedEvents : PeerError() {
        override val message: String = "User Rejected Events"
        override val code: Int = 5003
    }

    //Uncategorized errors

    data class NoMatchingTopic(val sequence: String, val topic: String) : PeerError() {
        override val message: String = "No matching $sequence with topic: $topic"
        override val code: Int = 1301
    }

    class UserDisconnected : PeerError() {
        override val message: String = "User disconnected"
        override val code: Int = 6000
    }

    data class SessionSettlementFailed(val reason: String) : PeerError() {
        override val message: String = "Invalid Session Settle Request: $reason"
        override val code: Int = 6001
    }

    data class Error(val reason: String, val errorCode: Int) : PeerError() {
        override val message: String = reason
        override val code: Int = errorCode
    }
}