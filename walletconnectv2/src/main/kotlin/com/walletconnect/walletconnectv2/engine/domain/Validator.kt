package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.*
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.engine.model.ValidationError
import com.walletconnect.walletconnectv2.util.Time
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, NamespaceVO.Proposal>, onError: (ValidationError) -> Unit) {
        when {
            !areProposalNamespacesKeysProperlyFormatted(namespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_MISSING_MESSAGE))
            !areChainIdsValid(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_CAIP_2_MESSAGE))
            !areChainsInMatchingNamespace(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE))
            !areExtensionChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_EXTENSION_CHAINS_MISSING_MESSAGE))
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespace(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Proposal>,
        onError: (ValidationError) -> Unit,
    ) {
        when {
            !areAllProposalNamespacesApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejected)
            !areAccountsNotEmpty(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_MISSING_MESSAGE))
            !areAccountIdsValid(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE))
            !areAllChainsApprovedWithAtLeastOneAccount(sessionNamespaces, proposalNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_MISSING_FOR_CHAINS_MESSAGE))
            !areAllMethodsApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejectedMethods)
            !areAllEventsApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejectedEvents)
            !areAccountsInMatchingNamespace(sessionNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE))
            !areExtensionAccountsNotEmpty(sessionNamespaces) -> onError(
                ValidationError.UserRejectedChains(NAMESPACE_EXTENSION_ACCOUNTS_MISSING_MESSAGE))
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespaceUpdate(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        onError: (ValidationError) -> Unit,
    ) {
        when {
            !areSessionNamespacesKeysProperlyFormatted(sessionNamespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areAccountsNotEmpty(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_MISSING_MESSAGE))
            !areAccountIdsValid(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE))
            !areAccountsInMatchingNamespace(sessionNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE))
            !areExtensionAccountsNotEmpty(sessionNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_EXTENSION_ACCOUNTS_MISSING_MESSAGE))
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithMethodAuthorisation(
        chainId: String,
        method: String,
        namespaces: Map<String, NamespaceVO.Session>,
        onError: (ValidationError) -> Unit,
    ) {
        allApprovedMethodsWithChains(namespaces).also { allApprovedMethodsWithChains ->
            if (allApprovedMethodsWithChains[method] == null || !allApprovedMethodsWithChains[method]!!.contains(chainId)) {
                onError(ValidationError.UnauthorizedMethod)
            }
        }
    }

    @JvmSynthetic
    internal inline fun validateChainIdWithEventAuthorisation(
        chainId: String,
        event: String,
        namespaces: Map<String, NamespaceVO.Session>,
        onError: (ValidationError) -> Unit,
    ) {
        allApprovedEventsWithChains(namespaces).also { allApprovedMethodsWithChains ->
            if (allApprovedMethodsWithChains[event] == null || !allApprovedMethodsWithChains[event]!!.contains(chainId)) {
                onError(ValidationError.UnauthorizedEvent)
            }
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionRequest(request: EngineDO.Request, onError: (ValidationError) -> Unit) {
        if (request.params.isEmpty() || request.method.isEmpty() || request.chainId.isEmpty() ||
            request.topic.isEmpty() || !isChainIdCAIP2Compliant(request.chainId)
        ) {
            onError(ValidationError.InvalidSessionRequest)
        }
    }


    @JvmSynthetic
    internal inline fun validateEvent(event: EngineDO.Event, onError: (ValidationError) -> Unit) {
        if (event.data.isEmpty() || event.name.isEmpty() || event.chainId.isEmpty() || !isChainIdCAIP2Compliant(event.chainId)) {
            onError(ValidationError.InvalidEvent)
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionExtend(newExpiry: Long, currentExpiry: Long, onError: (ValidationError) -> Unit) {
        val extendedExpiry = newExpiry - currentExpiry
        val maxExpiry = Time.weekInSeconds

        if (newExpiry <= currentExpiry || extendedExpiry > maxExpiry) {
            onError(ValidationError.InvalidExtendRequest)
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

        val relayData: String? = mapOfQueryParameters["relay-data"]

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
            }.plus(
                namespace.extensions?.flatMap { extension ->
                    extension.events.map { event ->
                        event to namespace.accounts.map { getChainFromAccount(it) }
                    }
                } ?: emptyList()
            )
        }.toMap()

    private fun allRequiredEventsWithChains(namespaces: Map<String, NamespaceVO.Proposal>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.chains
            }.plus(
                namespace.extensions?.flatMap { extension ->
                    extension.events.map { event -> event to namespace.chains }
                } ?: emptyList()
            )
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
        val (namespace: String, reference: String, _: String) = elements

        return "$namespace:$reference"
    }

    private const val NAMESPACE_REGEX: String = "^[-a-z0-9]{3,8}$"
    private const val REFERENCE_REGEX: String = "^[-a-zA-Z0-9]{1,32}$"
    private const val ACCOUNT_ADDRESS_REGEX: String = "^[a-zA-Z0-9]{1,64}$"
}