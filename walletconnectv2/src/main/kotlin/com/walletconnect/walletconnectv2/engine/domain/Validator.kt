package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.*
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    private fun areNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

    private fun areChainsNotEmpty(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.map { namespace -> namespace.chains }.all { chains -> chains.isNotEmpty() }

    private fun areChainIdsValid(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.flatMap { namespace -> namespace.chains }.all { chain -> isChainIdValid(chain) }

    private fun areChainsInMatchingNamespace(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.all { (key, namespace) -> namespace.chains.all { chain -> chain.contains(key, true) } }

    private fun areExtensionChainsNotEmpty(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.chains } }
            .all { extChains -> extChains.isNotEmpty() }

    //todo: add on Dapp side before sending session proposal
    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, NamespaceVO.Proposal>, onNamespaceError: (String) -> Unit) {
        when {
            !areNamespacesKeysProperlyFormatted(namespaces) -> onNamespaceError(NAMESPACE_EXTENSION_KEYS_CAIP_2_MESSAGE)
            !areChainsNotEmpty(namespaces) -> onNamespaceError(NAMESPACE_MISSING_CHAINS_MESSAGE)
            !areChainIdsValid(namespaces) -> onNamespaceError(NAMESPACE_CHAINS_CAIP_2_MESSAGE)
            !areChainsInMatchingNamespace(namespaces) -> onNamespaceError(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE)
            !areExtensionChainsNotEmpty(namespaces) -> onNamespaceError(NAMESPACE_EXTENSION_MISSING_CHAINS_MESSAGE)
        }
    }

    private fun areAllProposalNamespacesApproved(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    ): Boolean =
        sessionNamespaces.keys.containsAll(proposalNamespaces.keys)

    private fun areAccountsNotEmpty(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.map { namespace -> namespace.accounts }.all { accounts -> accounts.isNotEmpty() }

    private fun areAccountIdsValid(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.all { accounts -> isAccountIdValid(accounts) }

    private fun allApprovedMethodsWithChains(namespaces: Map<String, NamespaceVO.Session>) =
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

    private fun allRequiredMethodsWithChains(namespaces: Map<String, NamespaceVO.Proposal>) = namespaces.values.flatMap { namespace ->
        namespace.methods.map { method -> method to namespace.chains }.toMutableList().apply {
            if (namespace.extensions != null) {
                addAll(namespace.extensions.flatMap { extension -> extension.methods.map { method -> method to namespace.chains } })
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
            val chainsApproved = allApprovedMethodsWithChains[method] ?: run { return false }
            if (!chainsApproved.containsAll(chainsRequested)) {
                return false
            }
        }

        return true
    }

    private fun allApprovedEventsWithChains(namespaces: Map<String, NamespaceVO.Session>) =
        namespaces.values.flatMap { namespace ->
            namespace.events.map { event -> event to namespace.accounts.map { getChainFromAccount(it) } }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions.flatMap { extension ->
                        extension.events.map { event -> event to namespace.accounts.map { getChainFromAccount(it) } }
                    })
                }
            }
        }.toMap()

    private fun allRequiredEventsWithChains(namespaces: Map<String, NamespaceVO.Proposal>) = namespaces.values.flatMap { namespace ->
        namespace.events.map { event -> event to namespace.chains }.toMutableList().apply {
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
            val chainsApproved = allApprovedEventsWithChains[method] ?: run { return false }
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

    @JvmSynthetic
    private inline fun validateSessionNamespace(
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
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespace(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        clientParams: ClientParams,
        onNamespaceError: (String) -> Unit,
    ) {
        if (clientParams is PairingParamsVO.SessionProposeParams) {
            validateSessionNamespace(sessionNamespaces, clientParams.namespaces, onNamespaceError)
        } else {
            onNamespaceError(NAMESPACE_MISSING_PROPOSAL_MESSAGE)
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespaceUpdate(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        clientParams: ClientParams,
        onNamespaceError: (String) -> Unit,
    ) {
        if (clientParams is SessionParamsVO.UpdateNamespacesParams) {
            //TODO: how to validate minimal requirements of proposal namespaces? We should store ProposalNamespaces as well in SessionVO
//            validateSessionNamespace(sessionNamespaces, clientParams.namespaces, onNamespaceError)
        } else {
            onNamespaceError(NAMESPACE_MISSING_PROPOSAL_MESSAGE)
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

    //TODO: What was it for? Since we are using this in emit() to validate event, shouldn't there be the same logic for sessionRequest()?
    @JvmSynthetic
    internal fun validateEvent(event: EngineDO.Event, onInvalidEvent: (String) -> Unit) {
        if (event.data.isEmpty() || event.name.isEmpty() || event.chainId.isEmpty()) {
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
    internal fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, accountAddress: String) = elements

        return "$namespace:$reference"
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