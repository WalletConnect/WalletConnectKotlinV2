package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_CHAIN_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.EMPTY_RPC_METHODS_LIST_MESSAGE
import com.walletconnect.walletconnectv2.core.exceptions.WRONG_CHAIN_ID_FORMAT_MESSAGE
import com.walletconnect.walletconnectv2.engine.model.EngineDO

internal object Validator {

    internal fun validateSessionPermissions(permissions: EngineDO.SessionPermissions, onInvalidPermissions: (String) -> Unit) {
        when {
            isBlockchainValid(permissions) -> onInvalidPermissions(EMPTY_CHAIN_LIST_MESSAGE)
            isJsonRpcValid(permissions) -> onInvalidPermissions(EMPTY_RPC_METHODS_LIST_MESSAGE)
            permissions.notification?.type?.isEmpty() == true -> onInvalidPermissions(EMPTY_RPC_METHODS_LIST_MESSAGE)
            permissions.blockchain.chains.any { chainId -> !isChainIdValid(chainId) } -> onInvalidPermissions(WRONG_CHAIN_ID_FORMAT_MESSAGE)
        }
    }

    private fun isJsonRpcValid(permissions: EngineDO.SessionPermissions) =
        permissions.jsonRpc.methods.isEmpty() || permissions.jsonRpc.methods.any { method -> method.isBlank() }

    private fun isBlockchainValid(permissions: EngineDO.SessionPermissions) =
        permissions.blockchain.chains.isEmpty() || permissions.blockchain.chains.any { chain -> chain.isBlank() }

    private fun isChainIdValid(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val namespace = elements[0]
        val reference = elements[1]
        return "^[-a-z0-9]{3,8}$".toRegex().matches(namespace) && "^[-a-zA-Z0-9]{1,32}$".toRegex().matches(reference)
    }
}