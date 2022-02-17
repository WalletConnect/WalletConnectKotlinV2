package com.walletconnect.walletconnectv2.core.exceptions.peer

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType

data class PeerError(val error: Error) {

    val message: String = when (error) {
        is Error.InvalidUpdateRequest -> "Invalid ${error.sequence} update request"
        is Error.InvalidUpgradeRequest -> "Invalid ${error.sequence} update request"
        is Error.NoMatchingTopic -> "No matching ${error.sequence} with topic: ${error.topic}"
        is Error.UnauthorizedTargetChainId -> "Unauthorized Target ChainId Requested: ${error.chainId}"
        is Error.UnauthorizedJsonRpcMethod -> "Unauthorized JSON-RPC Method Requested: ${error.method}"
        is Error.UnauthorizedNotificationType -> "Unauthorized Notification Type Requested: ${error.type}"
        is Error.UnauthorizedUpdateRequest -> "Unauthorized ${error.sequence} update request"
        is Error.UnauthorizedUpgradeRequest -> "Unauthorized ${error.sequence} upgrade request"
        is Error.UnauthorizedMatchingController -> "Unauthorized: peer is also ${getPeerType(error)}"
    }

    val code: Int = when (error) {
        is Error.InvalidUpdateRequest -> 1003
        is Error.InvalidUpgradeRequest -> 1004
        is Error.NoMatchingTopic -> 1301
        is Error.UnauthorizedTargetChainId -> 3000
        is Error.UnauthorizedJsonRpcMethod -> 3001
        is Error.UnauthorizedNotificationType -> 3002
        is Error.UnauthorizedUpdateRequest -> 3003
        is Error.UnauthorizedUpgradeRequest -> 3004
        is Error.UnauthorizedMatchingController -> 3005
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
}