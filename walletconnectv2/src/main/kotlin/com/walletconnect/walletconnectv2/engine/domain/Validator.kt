package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    internal fun validateMethods(methods: List<String>, onInvalidJsonRpc: (String) -> Unit) {
        if (!areMethodsValid(methods)) {
            onInvalidJsonRpc(EMPTY_RPC_METHODS_LIST_MESSAGE)
        }
    }

    internal fun validateEvents(events: List<String>, onInvalidEvents: (String) -> Unit) {
        if (!areEventsValid(events)) {
            onInvalidEvents(INVALID_EVENTS_MESSAGE)
        }
    }

    internal fun validateCAIP2(namespaces: List<EngineDO.Namespace>, onInvalidChains: (String) -> Unit) {
        if (namespaces.any { namespace -> namespace.chains.any { chainId -> !isChainIdValid(chainId) } }) {
            onInvalidChains(WRONG_CHAIN_ID_FORMAT_MESSAGE)
        }
    }

    internal fun validateIfAccountsAreOnValidNetwork(accounts: List<String>, chains: List<String>, onInvalidAccounts: (String) -> Unit) {
        if (!areAccountsOnValidNetworks(accounts, chains)) {
            onInvalidAccounts(UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    internal fun validateCAIP10(chains: List<String>, onInvalidAccounts: (String) -> Unit) {
        when {
            !areAccountsNotEmpty(chains) -> onInvalidAccounts(EMPTY_ACCOUNT_LIST_MESSAGE)
            chains.any { accountId -> !isAccountIdValid(accountId) } -> onInvalidAccounts(WRONG_ACCOUNT_ID_FORMAT_MESSAGE)
        }
    }

    internal fun isChainListNotEmpty(chains: List<String>): Boolean =
        chains.isNotEmpty() && chains.any { chain -> chain.isNotEmpty() }

    internal fun validateEvent(event: EngineDO.Event, onInvalidEvent: (String) -> Unit) {
        if (event.data.isEmpty() || event.name.isEmpty() || event.chainId != null && event.chainId.isEmpty()) {
            onInvalidEvent(INVALID_EVENT_MESSAGE)
        }
    }

    internal fun validateEventAuthorization(session: SessionVO, eventName: String, onUnauthorizedEvent: (String) -> Unit) {
        if (!session.isSelfController && !session.events.contains(eventName)) {
            onUnauthorizedEvent(UNAUTHORIZED_EVENT_TYPE_MESSAGE)
        }
    }

    internal fun validateChainIdAuthorization(chainId: String?, chains: List<String>, onInvalidChainId: (String) -> Unit) {
        if (chainId != null && !chains.contains(chainId)) {
            onInvalidChainId(UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    internal fun validateSessionExtend(newExpiry: Long, currentExpiry: Long, onInvalidExtend: (String) -> Unit) {
        val extendedExpiry = newExpiry - currentExpiry
        val maxExpiry = Time.weekInSeconds

        if (newExpiry <= currentExpiry || extendedExpiry > maxExpiry) {
            onInvalidExtend(INVALID_EXTEND_TIME)
        }
    }

    internal fun validateWCUri(uri: String): EngineDO.WalletConnectUri? {
        if (!uri.startsWith("wc:")) return null
        val properUriString = when {
            uri.contains("wc://") -> uri
            uri.contains("wc:/") -> uri.replace("wc:/", "wc://")
            else -> uri.replace("wc:", "wc://")
        }

        val pairUri: URI = try {
            URI(properUriString)
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

    internal fun areMethodsValid(methods: List<String>): Boolean =
        methods.isNotEmpty() && methods.all { method -> method.isNotEmpty() }

    internal fun areEventsValid(events: List<String>): Boolean =
        events.isNotEmpty() && events.any { type -> type.isNotEmpty() }

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
        val (namespace: String, reference: String, accountAddress: String) = elements

        return NAMESPACE_REGEX.toRegex().matches(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    internal fun areAccountsOnValidNetworks(accountIds: List<String>, chains: List<String>): Boolean {
        if (!areAccountsNotEmpty(accountIds) || chains.isEmpty()) return false
        accountIds.forEach { accountId ->
            val elements = accountId.split(":")
            if (elements.isEmpty() || elements.size != 3) return false
            val (namespace: String, reference: String, _) = elements
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

    fun getChainIds(accountIds: List<String>): List<String> {
        return accountIds.map { accountId ->
            val (namespace: String, reference: String, _) = accountId.split(":")
            "$namespace:$reference"
        }
    }

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}