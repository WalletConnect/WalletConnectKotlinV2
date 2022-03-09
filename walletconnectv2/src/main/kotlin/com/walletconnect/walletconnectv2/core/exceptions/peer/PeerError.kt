package com.walletconnect.walletconnectv2.core.exceptions.peer

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType

data class PeerError(val error: Error) {

    val message: String = when (error) {
        is Error.InvalidUpdateRequest -> "Invalid ${error.sequence} update request"
        is Error.InvalidUpgradeRequest -> "Invalid ${error.sequence} update request"
        is Error.InvalidExtendRequest -> "Invalid ${error.sequence} extend request"
        is Error.NoMatchingTopic -> "No matching ${error.sequence} with topic: ${error.topic}"
        is Error.UnauthorizedTargetChainId -> "Unauthorized Target ChainId Requested: ${error.chainId}"
        is Error.UnauthorizedJsonRpcMethod -> "Unauthorized JSON-RPC Method Requested: ${error.method}"
        is Error.UnauthorizedNotificationType -> "Unauthorized Notification Type Requested: ${error.type}"
        is Error.UnauthorizedUpdateRequest -> "Unauthorized ${error.sequence} update request"
        is Error.UnauthorizedUpgradeRequest -> "Unauthorized ${error.sequence} upgrade request"
        is Error.UnauthorizedMatchingController -> "Unauthorized: peer is also ${getPeerType(error)}"
        is Error.UnauthorizedExtendRequest -> "Unauthorized ${error.sequence} extend request"
        is Error.UserError -> error.message
    }

    val code: Int = when (error) {
        is Error.InvalidUpdateRequest -> 1003
        is Error.InvalidUpgradeRequest -> 1004
        is Error.InvalidExtendRequest -> 1005
        is Error.NoMatchingTopic -> 1301
        is Error.UnauthorizedTargetChainId -> 3000
        is Error.UnauthorizedJsonRpcMethod -> 3001
        is Error.UnauthorizedNotificationType -> 3002
        is Error.UnauthorizedUpdateRequest -> 3003
        is Error.UnauthorizedUpgradeRequest -> 3004
        is Error.UnauthorizedExtendRequest -> 3005
        is Error.UnauthorizedMatchingController -> 3100
        is Error.UserError -> error.code
    }

    private fun getPeerType(error: Error.UnauthorizedMatchingController): String =
        if (error.isController) ControllerType.CONTROLLER.type else ControllerType.NON_CONTROLLER.type
}

sealed class Error {
    data class InvalidUpdateRequest(val sequence: String) : Error()
    data class InvalidUpgradeRequest(val sequence: String) : Error()
    data class NoMatchingTopic(val sequence: String, val topic: String) : Error()
    data class UnauthorizedTargetChainId(val chainId: String) : Error()
    data class UnauthorizedJsonRpcMethod(val method: String) : Error()
    data class UnauthorizedNotificationType(val type: String) : Error()
    data class UnauthorizedUpdateRequest(val sequence: String) : Error()
    data class UnauthorizedUpgradeRequest(val sequence: String) : Error()
    data class UnauthorizedMatchingController(val isController: Boolean) : Error()
    data class UnauthorizedExtendRequest(val sequence: String) : Error()
    data class InvalidExtendRequest(val sequence: String) : Error()
    data class UserError(val message: String, val code: Int) : Error()
}