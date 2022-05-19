package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, NamespaceVO.Proposal>, onNamespaceError: (String) -> Unit) {
        when {
            !areProposalNamespacesKeysProperlyFormatted(namespaces) -> onNamespaceError(NAMESPACE_EXTENSION_KEYS_CAIP_2_MESSAGE)
            !areChainsNotEmpty(namespaces) -> onNamespaceError(NAMESPACE_MISSING_CHAINS_MESSAGE)
            !areChainIdsValid(namespaces) -> onNamespaceError(NAMESPACE_CHAINS_CAIP_2_MESSAGE)
            !areChainsInMatchingNamespace(namespaces) -> onNamespaceError(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE)
            !areExtensionChainsNotEmpty(namespaces) -> onNamespaceError(NAMESPACE_EXTENSION_MISSING_CHAINS_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespace(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
        onNamespaceError: (String) -> Unit,
    ) {
        when {
            !areAllProposalNamespacesApproved(sessionNamespaces, proposalNamespaces) -> onNamespaceError(NAMESPACE_KEYS_MISSING_MESSAGE)
            !areAccountsNotEmpty(sessionNamespaces) -> onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_MESSAGE)
            !areAccountIdsValid(sessionNamespaces) -> onNamespaceError(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE)
            !areAllChainsApprovedWithAtLeastOneAccount(sessionNamespaces, proposalNamespaces) ->
                onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_FOR_CHAINS_MESSAGE)
            !areAllMethodsApproved(sessionNamespaces, proposalNamespaces) -> onNamespaceError(NAMESPACE_MISSING_METHODS_MESSAGE)
            !areAllEventsApproved(sessionNamespaces, proposalNamespaces) -> onNamespaceError(NAMESPACE_MISSING_EVENTS_MESSAGE)
            !areAccountsInMatchingNamespace(sessionNamespaces) -> onNamespaceError(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE)
            !areExtensionAccountsNotEmpty(sessionNamespaces) -> onNamespaceError(NAMESPACE_EXTENSION_MISSING_ACCOUNTS_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespaceUpdate(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        onNamespaceError: (String) -> Unit,
    ) {
        when {
            !areSessionNamespacesKeysProperlyFormatted(sessionNamespaces) -> onNamespaceError(NAMESPACE_EXTENSION_KEYS_CAIP_2_MESSAGE)
            !areAccountsNotEmpty(sessionNamespaces) -> onNamespaceError(NAMESPACE_MISSING_ACCOUNTS_MESSAGE)
            !areAccountIdsValid(sessionNamespaces) -> onNamespaceError(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE)
            !areAccountsInMatchingNamespace(sessionNamespaces) -> onNamespaceError(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE)
            !areExtensionAccountsNotEmpty(sessionNamespaces) -> onNamespaceError(NAMESPACE_EXTENSION_MISSING_ACCOUNTS_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithMethodAuthorisation(
        chainId: String,
        method: String,
        namespaces: Map<String, NamespaceVO.Session>,
        onInvalidChainId: (String) -> Unit,
    ) {
        allApprovedMethodsWithChains(namespaces).also { allApprovedMethodsWithChains ->
            if (allApprovedMethodsWithChains[method] == null || !allApprovedMethodsWithChains[method]!!.contains(chainId)) {
                onInvalidChainId(UNAUTHORIZED_CHAIN_ID_OR_METHOD_MESSAGE)
            }
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithEventAuthorisation(
        chainId: String,
        event: String,
        namespaces: Map<String, NamespaceVO.Session>,
        onInvalidChainId: (String) -> Unit,
    ) {
        allApprovedEventsWithChains(namespaces).also { allApprovedMethodsWithChains ->
            if (allApprovedMethodsWithChains[event] == null || !allApprovedMethodsWithChains[event]!!.contains(chainId)) {
                onInvalidChainId(UNAUTHORIZED_CHAIN_ID_OR_EVENT_MESSAGE)
            }
        }
    }

    @JvmSynthetic
    internal fun validateSessionRequest(request: EngineDO.Request, onInvalidRequest: (String) -> Unit) {
        if (request.params.isEmpty() || request.method.isEmpty() || request.chainId.isEmpty() ||
            request.topic.isEmpty() || !isChainIdCAIP2Compliant(request.chainId)
        ) {
            onInvalidRequest(INVALID_REQUEST_MESSAGE)
        }
    }


    @JvmSynthetic
    internal fun validateEvent(event: EngineDO.Event, onInvalidEvent: (String) -> Unit) {
        if (event.data.isEmpty() || event.name.isEmpty() || event.chainId.isEmpty() || !isChainIdCAIP2Compliant(event.chainId)) {
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

    private fun areProposalNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

    private fun areChainsNotEmpty(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.map { namespace -> namespace.chains }.all { chains -> chains.isNotEmpty() }

    private fun areChainIdsValid(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.flatMap { namespace -> namespace.chains }.all { chain -> isChainIdCAIP2Compliant(chain) }

    private fun areChainsInMatchingNamespace(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.all { (key, namespace) -> namespace.chains.all { chain -> chain.contains(key, true) } }

    private fun areExtensionChainsNotEmpty(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.chains } }
            .all { extChain -> extChain.isNotEmpty() }

    private fun areAllProposalNamespacesApproved(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    ): Boolean = sessionNamespaces.keys.containsAll(proposalNamespaces.keys)

    private fun areAccountsNotEmpty(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.map { namespace -> namespace.accounts }.all { accounts -> accounts.isNotEmpty() }

    private fun areAccountIdsValid(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.all { accounts -> isAccountIdCAIP10Compliant(accounts) }

    private fun allApprovedMethodsWithChains(namespaces: Map<String, NamespaceVO.Session>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.methods.map { method ->
                method to namespace.accounts.map { getChainFromAccount(it) }
            }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions.flatMap { extension ->
                        extension.methods.map { method ->
                            method to namespace.accounts.map { getChainFromAccount(it) }
                        }
                    })
                }
            }
        }.toMap()

    private fun allRequiredMethodsWithChains(namespaces: Map<String, NamespaceVO.Proposal>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.methods.map { method ->
                method to namespace.chains
            }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions.flatMap { extension ->
                        extension.methods.map { method -> method to namespace.chains }
                    })
                }
            }
        }.toMap()

    private fun areAllMethodsApproved(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    ): Boolean {
        val allApprovedMethodsWithChains = allApprovedMethodsWithChains(sessionNamespaces)
        val allRequiredMethodsWithChains = allRequiredMethodsWithChains(proposalNamespaces)

        allRequiredMethodsWithChains.forEach { (method, chainsRequested) ->
            val chainsApproved = allApprovedMethodsWithChains[method] ?: return false
            if (!chainsApproved.containsAll(chainsRequested)) {
                return false
            }
        }
        return true
    }

    private fun allApprovedEventsWithChains(namespaces: Map<String, NamespaceVO.Session>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.accounts.map { getChainFromAccount(it) }
            }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions.flatMap { extension ->
                        extension.events.map { event ->
                            event to namespace.accounts.map { getChainFromAccount(it) }
                        }
                    })
                }
            }
        }.toMap()

    private fun allRequiredEventsWithChains(namespaces: Map<String, NamespaceVO.Proposal>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.chains
            }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions.flatMap { extension ->
                        extension.events.map { event -> event to namespace.chains }
                    })
                }
            }
        }.toMap()

    private fun areAllEventsApproved(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    ): Boolean {
        val allApprovedEventsWithChains = allApprovedEventsWithChains(sessionNamespaces)
        val allRequiredEventsWithChains = allRequiredEventsWithChains(proposalNamespaces)

        allRequiredEventsWithChains.forEach { (method, chainsRequested) ->
            val chainsApproved = allApprovedEventsWithChains[method] ?: return false
            if (!chainsApproved.containsAll(chainsRequested)) {
                return false
            }
        }
        return true
    }

    private fun areAllChainsApprovedWithAtLeastOneAccount(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    ): Boolean =
        sessionNamespaces.values.flatMap { namespace -> namespace.accounts.map { it.substringBeforeLast(":") } }
            .containsAll(proposalNamespaces.values.flatMap { namespace -> namespace.chains })

    private fun areAccountsInMatchingNamespace(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.all { (key, namespace) -> namespace.accounts.all { it.contains(key) } }

    private fun areExtensionAccountsNotEmpty(namespaces: Map<String, NamespaceVO.Session>): Boolean =
        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.accounts } }
            .all { extAccount -> extAccount.isNotEmpty() }

    private fun areSessionNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO.Session>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

    @JvmSynthetic
    internal fun isChainIdCAIP2Compliant(chainId: String): Boolean {
        val elements: List<String> = chainId.split(":")
        if (elements.isEmpty() || elements.size != 2) return false
        val (namespace: String, reference: String) = elements
        return NAMESPACE_REGEX.toRegex().matches(namespace) && REFERENCE_REGEX.toRegex().matches(reference)
    }

    @JvmSynthetic
    internal fun isAccountIdCAIP10Compliant(accountId: String): Boolean {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return false
        val (namespace: String, reference: String, accountAddress: String) = elements

        return NAMESPACE_REGEX.toRegex().matches(namespace) &&
                REFERENCE_REGEX.toRegex().matches(reference) &&
                ACCOUNT_ADDRESS_REGEX.toRegex().matches(accountAddress)
    }

    @JvmSynthetic
    internal fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, accountAddress: String) = elements

        return "$namespace:$reference"
    }

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}