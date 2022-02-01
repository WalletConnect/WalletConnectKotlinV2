package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_CHAIN_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_RPC_METHODS_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.INVALID_NOTIFICATIONS_TYPES_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.WRONG_CHAIN_ID_FORMAT_MESSAGE
import com.walletconnect.walletconnectv2.engine.model.EngineDO

internal object Validator {

    internal fun validateSessionPermissions(permissions: EngineDO.SessionPermissions, onInvalidPermissions: (String) -> Unit) {
        when {
            !isBlockchainValid(permissions.blockchain) -> onInvalidPermissions(EMPTY_CHAIN_LIST_MESSAGE)
            !isJsonRpcValid(permissions.jsonRpc) -> onInvalidPermissions(EMPTY_RPC_METHODS_LIST_MESSAGE)
            permissions.notification != null && !areNotificationTypesValid(permissions.notification) ->
                onInvalidPermissions(INVALID_NOTIFICATIONS_TYPES_MESSAGE)
            permissions.blockchain.chains.any { chainId -> !isChainIdValid(chainId) } -> onInvalidPermissions(WRONG_CHAIN_ID_FORMAT_MESSAGE)
        }
    }

    internal fun isJsonRpcValid(jsonRpc: EngineDO.JsonRpc): Boolean =
        jsonRpc.methods.isNotEmpty() && jsonRpc.methods.all { method -> method.isNotEmpty() }

    internal fun isBlockchainValid(blockchain: EngineDO.Blockchain) =
        blockchain.chains.isNotEmpty() && blockchain.chains.any { chain -> chain.isNotEmpty() }

    internal fun areNotificationTypesValid(notification: EngineDO.Notifications): Boolean =
        notification.types.isNotEmpty() && notification.types.any { type -> type.isNotEmpty() }

    internal fun isChainIdValid(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val namespace = elements[0]
        val reference = elements[1]
        return "^[-a-z0-9]{3,8}$".toRegex().matches(namespace) && "^[-a-zA-Z0-9]{1,32}$".toRegex().matches(reference)
    }
}