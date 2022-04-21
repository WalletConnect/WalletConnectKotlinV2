package com.walletconnect.walletconnectv2.core.exceptions.peer

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType

sealed class PeerError {
    abstract val message: String
    abstract val code: Int

    data class InvalidUpdateAccountsRequest(val sequence: String) : PeerError() {
        override val message = "Invalid $sequence update accounts request"
        override val code: Int = 1003
    }

    data class InvalidUpdateMethodsRequest(val sequence: String) : PeerError() {
        override val message = "Invalid $sequence update methods request"
        override val code: Int = 1004
    }

    data class InvalidUpdateEventsRequest(val sequence: String) : PeerError() {
        override val message = "Invalid $sequence update events request"
        override val code: Int = 1005
    }

    data class InvalidUpdateExpiryRequest(val sequence: String) : PeerError() {
        override val message = "Invalid $sequence update expiry request"
        override val code: Int = 1006
    }

    data class NoMatchingTopic(val sequence: String, val topic: String) : PeerError() {
        override val message: String = "No matching $sequence with topic: $topic"
        override val code: Int = 1301
    }

    data class UnauthorizedTargetChainId(val chainId: String) : PeerError() {
        override val message: String = "Unauthorized Target ChainId Requested: $chainId"
        override val code: Int = 3000
    }

    data class UnauthorizedJsonRpcMethod(val method: String) : PeerError() {
        override val message: String = "Unauthorized JSON-RPC Method Requested: $method"
        override val code: Int = 3001
    }

    data class UnauthorizedEventRequest(val name: String) : PeerError() {
        override val message: String = "Unauthorized event name requested: $name"
        override val code: Int = 3002
    }

    data class UnauthorizedUpdateAccountsRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence update accounts request"
        override val code: Int = 3003
    }

    data class UnauthorizedUpdateEventsRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence update accounts request"
        override val code: Int = 3004
    }

    data class UnauthorizedUpdateMethodsRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence update methods request"
        override val code: Int = 3005
    }

    data class UnauthorizedUpdateExpiryRequest(val sequence: String) : PeerError() {
        override val message: String = "Unauthorized $sequence update expiry request"
        override val code: Int = 3006
    }

    data class UnauthorizedMatchingController(val isController: Boolean) : PeerError() {
        private val peerType: String = if (isController) ControllerType.CONTROLLER.type else ControllerType.NON_CONTROLLER.type
        override val message: String = "Unauthorized: peer is also $peerType"
        override val code: Int = 3100
    }

    data class Error(val reason: String, val errorCode: Int) : PeerError() {
        override val message: String = reason
        override val code: Int = errorCode
    }
}