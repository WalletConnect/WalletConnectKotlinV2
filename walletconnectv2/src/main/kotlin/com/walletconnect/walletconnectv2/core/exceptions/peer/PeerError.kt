package com.walletconnect.walletconnectv2.core.exceptions.peer

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType

data class PeerError(val error: Error) {

    val message: String = when (error) {
        is Error.InvalidUpdateAccountsRequest -> "Invalid ${error.sequence} update accounts request"
        is Error.InvalidUpdateMethodsRequest -> "Invalid ${error.sequence} update methods request"
        is Error.InvalidUpdateEventsRequest -> "Invalid ${error.sequence} update events request"
        is Error.InvalidUpdateExpiryRequest -> "Invalid ${error.sequence} extend request"
        is Error.NoMatchingTopic -> "No matching ${error.sequence} with topic: ${error.topic}"
        is Error.UnauthorizedTargetChainId -> "Unauthorized target chain id requested: ${error.chainId}"
        is Error.UnauthorizedJsonRpcMethod -> "Unauthorized JSON-RPC method requested: ${error.method}"
        is Error.UnauthorizedEventRequest -> "Unauthorized event name requested: ${error.type}"
        is Error.UnauthorizedUpdateAccountsRequest -> "Unauthorized ${error.sequence} update accounts request"
        is Error.UnauthorizedUpdateMethodsRequest -> "Unauthorized ${error.sequence} update methods request"
        is Error.UnauthorizedUpdateEventsRequest -> "Unauthorized ${error.sequence} update events request"
        is Error.UnauthorizedUpdateExpiryRequest -> "Unauthorized ${error.sequence} update expiry request"
        is Error.UnauthorizedMatchingController -> "Unauthorized: peer is also ${getPeerType(error)}"
        is Error.UserError -> error.message
    }

    val code: Int = when (error) {
        is Error.InvalidUpdateAccountsRequest -> 1003
        is Error.InvalidUpdateMethodsRequest -> 1004
        is Error.InvalidUpdateEventsRequest -> 1005
        is Error.InvalidUpdateExpiryRequest -> 1006
        is Error.NoMatchingTopic -> 1301
        is Error.UnauthorizedTargetChainId -> 3000
        is Error.UnauthorizedJsonRpcMethod -> 3001
        is Error.UnauthorizedEventRequest -> 3002
        is Error.UnauthorizedUpdateAccountsRequest -> 3003
        is Error.UnauthorizedUpdateEventsRequest -> 3004
        is Error.UnauthorizedUpdateMethodsRequest -> 3005
        is Error.UnauthorizedUpdateExpiryRequest -> 3006
        is Error.UnauthorizedMatchingController -> 3100
        is Error.UserError -> error.code
    }

    private fun getPeerType(error: Error.UnauthorizedMatchingController): String =
        if (error.isController) ControllerType.CONTROLLER.type else ControllerType.NON_CONTROLLER.type
}

sealed class Error {
    data class InvalidUpdateAccountsRequest(val sequence: String) : Error()
    data class InvalidUpdateMethodsRequest(val sequence: String) : Error()
    data class InvalidUpdateEventsRequest(val sequence: String) : Error()
    data class InvalidUpdateExpiryRequest(val sequence: String) : Error()

    data class NoMatchingTopic(val sequence: String, val topic: String) : Error()
    data class UnauthorizedTargetChainId(val chainId: String) : Error()
    data class UnauthorizedJsonRpcMethod(val method: String) : Error()

    data class UnauthorizedEventRequest(val type: String) : Error()
    data class UnauthorizedUpdateAccountsRequest(val sequence: String) : Error()
    data class UnauthorizedUpdateMethodsRequest(val sequence: String) : Error()
    data class UnauthorizedUpdateEventsRequest(val sequence: String) : Error()
    data class UnauthorizedUpdateExpiryRequest(val sequence: String) : Error()

    data class UnauthorizedMatchingController(val isController: Boolean) : Error()
    data class UserError(val message: String, val code: Int) : Error()
}