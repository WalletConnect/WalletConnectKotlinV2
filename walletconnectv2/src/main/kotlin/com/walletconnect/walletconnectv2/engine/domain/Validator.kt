package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

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

    internal fun validateIfChainIdsIncludedInPermission(accounts: List<String>, chains: List<String>, onInvalidAccounts: (String) -> Unit) {
        if (!areChainIdsIncludedInPermissions(accounts, chains)) {
            onInvalidAccounts(UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    internal fun validateCAIP10(accounts: List<String>, onInvalidAccounts: (String) -> Unit) {
        when {
            !areAccountsNotEmpty(accounts) -> onInvalidAccounts(EMPTY_ACCOUNT_LIST_MESSAGE)
            accounts.any { accountId -> !isAccountIdValid(accountId) } -> onInvalidAccounts(WRONG_ACCOUNT_ID_FORMAT_MESSAGE)
        }
    }

    internal fun validateNotification(notification: EngineDO.Notification, onInvalidNotification: (String) -> Unit) {
        if (notification.data.isEmpty() || notification.type.isEmpty()) onInvalidNotification(INVALID_NOTIFICATION_MESSAGE)
    }

    internal fun validateNotificationAuthorization(session: SessionVO, type: String, onUnauthorizedNotification: (String) -> Unit) {
        if (!session.isSelfController && session.types?.contains(type) == false) {
            onUnauthorizedNotification(UNAUTHORIZED_NOTIFICATION_TYPE_MESSAGE)
        }
    }

    internal fun validateChainIdAuthorization(chainId: String?, chains: List<String>, onInvalidChainId: (String) -> Unit) {
        if (chainId != null) {
            if (!chains.contains(chainId)) onInvalidChainId(UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    internal fun validateProposalFields(sessionProposal: EngineDO.SessionProposal, onInvalidProposal: (String) -> Unit) {
        with(sessionProposal) {
            if (name.isEmpty() || description.isEmpty() || url.isEmpty() || icons.isEmpty() || chains.isEmpty() || methods.isEmpty() ||
                topic.isEmpty() || publicKey.isEmpty() || relayProtocol.isEmpty()
            ) {
                onInvalidProposal(INVALID_SESSION_PROPOSAL_MESSAGE)
            }
        }
    }

    internal fun validateSessionExtend(newExpiry: Long, currentExpiry: Long, onInvalidExtend: (String) -> Unit) {
        if (newExpiry <= currentExpiry && newExpiry > Time.week) {
            onInvalidExtend(INVALID_EXTEND_TIME)
        }
    }

    internal fun validatePairingExtend(newExpiry: Long, currentExpiry: Long, onInvalidExtend: (String) -> Unit) {
        if (newExpiry <= currentExpiry && newExpiry > Time.month) {
            onInvalidExtend(INVALID_EXTEND_TIME)
        }
    }

    internal fun validateWCUri(uri: String): EngineDO.WalletConnectUri? {
        if (!uri.startsWith("wc:")) return null
        val properUriString = if (uri.contains("wc://")) uri else uri.replace("wc:", "wc://")
        val pairUri: URI
        try {
            pairUri = URI(properUriString)
        } catch (e: URISyntaxException) {
            return null
        }

        if (pairUri.userInfo.isEmpty()) return null
        val mapOfQueryParameters: Map<String, String> =
            pairUri.query.split("&").associate { query -> query.substringBefore("=") to query.substringAfter("=") }

        var relayProtocol = ""
        mapOfQueryParameters["relay-protocol"]?.let { relayProtocol = it } ?: return null
        if (relayProtocol.isEmpty()) return null

        var relayData: String? = ""
        relayData = mapOfQueryParameters["relay-data"]

        var symKey = ""
        mapOfQueryParameters["symKey"]?.let { symKey = it } ?: return null
        if (symKey.isEmpty()) return null

        return EngineDO.WalletConnectUri(
            topic = TopicVO(pairUri.userInfo),
            relay = RelayProtocolOptionsVO(protocol = relayProtocol, data = relayData),
            symKey = SecretKey(symKey)
        )
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
        return NAMESPACE_REGEX.toRegex().matches(namespace) && REFERENCE_REGEX.toRegex().matches(reference)
    }

    internal fun areAccountsNotEmpty(accounts: List<String>): Boolean =
        accounts.isNotEmpty() && accounts.all { method -> method.isNotEmpty() }

    internal fun isAccountIdValid(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace, reference, accountAddress) = splitAccountId(elements)
        return NAMESPACE_REGEX.toRegex().matches(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    internal fun areChainIdsIncludedInPermissions(accountIds: List<String>, chains: List<String>): Boolean {
        if (!areAccountsNotEmpty(accountIds) || chains.isEmpty()) return false
        accountIds.forEach { accountId ->
            val elements = accountId.split(":")
            if (elements.isEmpty() || elements.size != 3) return false
            val (namespace, reference, _) = splitAccountId(elements)

            val chainId = "$namespace:$reference"
            if (!chains.contains(chainId)) return false
        }
        return true
    }

    private fun splitAccountId(elements: List<String>): Triple<String, String, String> {
        val namespace = elements[0]
        val reference = elements[1]
        val accountAddress = elements[2]
        return Triple(namespace, reference, accountAddress)
    }

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}