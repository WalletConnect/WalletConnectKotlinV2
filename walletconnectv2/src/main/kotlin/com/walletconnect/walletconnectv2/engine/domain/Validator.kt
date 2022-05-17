package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    // TODO: Verify validation is correct
    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, NamespaceVO.Proposal>, onNamespaceError: (String) -> Unit) {
        if (!namespaces.values.map { namespace -> namespace.chains }.all { chains -> chains.isNotEmpty() }) {
            onNamespaceError(NAMESPACE_MISSING_CHAINS_MESSAGE)
        } else if (!namespaces.values.flatMap { namespace -> namespace.chains }.all { chain -> isChainIdValid(chain) }) {
            onNamespaceError(NAMESPACE_CHAINS_CAIP_2_MESSAGE)
        } else if (!namespaces.all { (key, namespace) -> namespace.chains.all { chain -> chain.contains(key, true) } }) {
            onNamespaceError(NAMESPACE_MISSING_PREFIX_MESSAGE)
        } else if (!namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.chains } }.all { extChains -> extChains.isNotEmpty() }) {
            onNamespaceError(NAMESPACE_EXTENSION_MISSING_CHAINS_MESSAGE)
        } else if (!namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }) {
            onNamespaceError(NAMESPACE_EXTENSION_KEYS_CAIP_2_MESSAGE)
        }
    }

    // TODO: Verify validation is correct
    @JvmSynthetic
    internal inline fun validateSessionNamespace(sessionNamespaces: Map<String, NamespaceVO.Session>, proposalParams: ClientParams, onNamespaceError: (String) -> Unit) {
        if (proposalParams is PairingParamsVO.SessionProposeParams) {
            if (!sessionNamespaces.values.map { namespace -> namespace.accounts }.all { accounts -> accounts.isNotEmpty() }) {
                onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.all { accounts -> isAccountIdValid(accounts) }) {
                onNamespaceError(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.methods }.containsAll(proposalParams.namespaces.values.flatMap { namespace -> namespace.methods })) {
                onNamespaceError(NAMESPACE_MISSING_METHODS_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.accounts.map { it.substringBeforeLast(":") } }
                    .containsAll(proposalParams.namespaces.values.flatMap { namespace -> namespace.chains })) {
                onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_FOR_CHAINS_MESSAGE)
            } else if (!sessionNamespaces.all { (key, namespace) -> namespace.accounts.all { it.contains(key) } }) {
                onNamespaceError(NAMESPACE_ACCOUNTS_MISSING_CHAIN_MESSAGE)
            } else if (sessionNamespaces.keys.containsAll(proposalParams.namespaces.keys)) {
                onNamespaceError(NAMESPACE_KEYS_MISSING_MESSAGE)
            } else if (false) {
                // TODO: 2.10 -> check that all extension chains and methods are included in either the super set or in extensions
                onNamespaceError(NAMESPACE_EXTENSION_METHODS_MISSING_MESSAGE)
            } else if (false) {
                // TODO: 2.11 -> check that all extension chains and events are included in either the super set or in extensions
                onNamespaceError(NAMESPACE_EXTENSION_EVENTS_MISSING_MESSAGE)
            }
        } else {
            onNamespaceError(NAMESPACE_MISSING_PROPOSAL_MESSAGE)
        }
    }

    // TODO: Verify validation is correct
    @JvmSynthetic
    internal inline fun validateSessionNamespaceUpdate(sessionNamespaces: Map<String, NamespaceVO.Session>, proposalParams: ClientParams, onNamespaceError: (String) -> Unit) {
        if (proposalParams is SessionParamsVO.UpdateNamespacesParams) {
            if (!sessionNamespaces.values.map { namespace -> namespace.accounts }.all { accounts -> accounts.isNotEmpty() }) {
                onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.all { accounts -> isAccountIdValid(accounts) }) {
                onNamespaceError(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.methods }.containsAll(proposalParams.namespaces.values.flatMap { namespace -> namespace.methods })) {
                onNamespaceError(NAMESPACE_MISSING_METHODS_MESSAGE)
            } else if (!sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.containsAll(proposalParams.namespaces.values.flatMap { namespace -> namespace.accounts })) {
                onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_FOR_CHAINS_MESSAGE)
            } else if (!sessionNamespaces.all { (key, namespace) -> namespace.accounts.all { it.contains(key) } }) {
                onNamespaceError(NAMESPACE_ACCOUNTS_MISSING_CHAIN_MESSAGE)
            } else if (sessionNamespaces.keys.containsAll(proposalParams.namespaces.keys)) {
                onNamespaceError(NAMESPACE_KEYS_MISSING_MESSAGE)
            } else if (false) {
                // TODO: 2.10 -> check that all extension chains and methods are included in either the super set or in extensions
                onNamespaceError(NAMESPACE_EXTENSION_METHODS_MISSING_MESSAGE)
            } else if (false) {
                // TODO: 2.11 -> check that all extension chains and events are included in either the super set or in extensions
                onNamespaceError(NAMESPACE_EXTENSION_EVENTS_MISSING_MESSAGE)
            }
        } else {
            onNamespaceError(NAMESPACE_MISSING_PROPOSAL_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithMethodAuthorisation(chainId: String?, method: String, namespaces: Map<String, NamespaceVO.Session>, onInvalidChainId: (String) -> Unit) {
        if (chainId == null ||
            !namespaces.values.map { it.accounts to it.methods }.any { (accounts, methods) -> accounts.map { it.substringBeforeLast(":") }.contains(chainId) && methods.contains(method) } ||
            !namespaces.values.filter { it.extensions != null }.map { namespace -> namespace.extensions!!.flatMap { it.accounts } to namespace.extensions.flatMap { it.methods } }
                .any { (accounts, methods) -> accounts.map { it.substringBeforeLast(":") }.contains(chainId) && methods.contains(method) }
        ) {
            onInvalidChainId(UNAUTHORIZED_CHAIN_ID_OR_METHOD_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithEventAuthorisation(chainId: String?, event: String, namespaces: Map<String, NamespaceVO.Session>, onInvalidChainId: (String) -> Unit) {
        if (chainId == null ||
            !namespaces.values.map { it.accounts to it.events }.any { (accounts, events) -> accounts.map { it.substringBeforeLast(":") }.contains(chainId) && events.contains(event) } ||
            !namespaces.values.filter { it.extensions != null }.map { namespace -> namespace.extensions!!.flatMap { it.accounts } to namespace.extensions.flatMap { it.events } }.any { (accounts, events) -> accounts.map { it.substringBeforeLast(":") }.contains(chainId) && events.contains(event) }
        ) {
            onInvalidChainId(UNAUTHORIZED_CHAIN_ID_OR_EVENT_MESSAGE)
        }
    }

    @JvmSynthetic
    internal fun validateChainIdAuthorization(chainId: String?, namespaces: Map<String, NamespaceVO.Session>, onInvalidChainId: (String) -> Unit) {

//        if (chainId == null || !true) {
//            onInvalidChainId(UNAUTHORIZED_CHAIN_ID_MESSAGE)
//        }
    }

    @JvmSynthetic
    internal fun validateMethodAuthorisation(namespaces: Map<String, NamespaceVO.Session>, method: String, onInvalidMethod: (String) -> Unit) {
//        if (!namespaces.any { namespace -> namespace.methods.contains(method) }) {
//            onInvalidMethod(UNAUTHORIZED_METHOD)
//        }
    }

    @JvmSynthetic
    internal fun validateCAIP2(namespaces: Map<String, EngineDO.Namespace.Proposal>, onInvalidChains: (String) -> Unit) {
        if (namespaces.values.any { namespace -> namespace.chains.any { chainId -> !isChainIdValid(chainId) } }) {
            onInvalidChains(WRONG_CHAIN_ID_FORMAT_MESSAGE)
        }
    }

    @JvmSynthetic
    internal fun validateIfAccountsAreOnValidNetwork(accounts: List<String>, chains: List<String>, onInvalidAccounts: (String) -> Unit) {
        if (!areAccountsOnValidNetworks(accounts, chains)) {
            onInvalidAccounts(UNAUTHORIZED_CHAIN_ID_MESSAGE)
        }
    }

    @JvmSynthetic
    internal fun validateCAIP10(chains: List<String>, onInvalidAccounts: (String) -> Unit) {
        when {
            !areAccountsNotEmpty(chains) -> onInvalidAccounts(EMPTY_ACCOUNT_LIST_MESSAGE)
            chains.any { accountId -> !isAccountIdValid(accountId) } -> onInvalidAccounts(WRONG_ACCOUNT_ID_FORMAT_MESSAGE)
        }
    }

    @JvmSynthetic
    internal fun validateEvent(event: EngineDO.Event, onInvalidEvent: (String) -> Unit) {
        if (event.data.isEmpty() || event.name.isEmpty() || event.chainId != null && event.chainId.isEmpty()) {
            onInvalidEvent(INVALID_EVENT_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionExtend(newExpiry: Long, currentExpiry: Long, onInvalidExtend: (String) -> Unit) {
        val extendedExpiry = newExpiry - currentExpiry
        val maxExpiry = Time.weekInSeconds

        if (newExpiry <= currentExpiry || extendedExpiry > maxExpiry) {
            onInvalidExtend(INVALID_EXTEND_TIME)
        }
    }

    @JvmSynthetic
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

    @JvmSynthetic
    internal fun isChainIdValid(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val (namespace: String, reference: String) = elements
        return NAMESPACE_REGEX.toRegex().matches(namespace) && REFERENCE_REGEX.toRegex().matches(reference)
    }

    @JvmSynthetic
    internal fun areAccountsNotEmpty(accounts: List<String>): Boolean =
        accounts.isNotEmpty() && accounts.all { method -> method.isNotBlank() }

    @JvmSynthetic
    internal fun isAccountIdValid(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace: String, reference: String, accountAddress: String) = elements

        return NAMESPACE_REGEX.toRegex().matches(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    @JvmSynthetic
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

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}