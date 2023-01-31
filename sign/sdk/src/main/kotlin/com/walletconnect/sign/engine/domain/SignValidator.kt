@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.utils.CoreValidator.NAMESPACE_REGEX
import com.walletconnect.android.internal.utils.CoreValidator.isAccountIdCAIP10Compliant
import com.walletconnect.android.internal.utils.CoreValidator.isChainIdCAIP2Compliant
import com.walletconnect.android.internal.utils.WEEK_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE
import com.walletconnect.sign.common.exceptions.NAMESPACE_ACCOUNTS_MISSING_MESSAGE
import com.walletconnect.sign.common.exceptions.NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.ValidationError
import java.net.URI
import java.net.URISyntaxException

internal object SignValidator {

    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, NamespaceVO.Required>, onError: (ValidationError) -> Unit) {
        when {
            !areProposalNamespacesKeysProperlyFormatted(namespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
//            !areChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_MISSING_MESSAGE))
//            !areChainIdsValid(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_CAIP_2_MESSAGE))
//            !areChainsInMatchingNamespace(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE))
//            !areExtensionChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_EXTENSION_CHAINS_MISSING_MESSAGE))
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespace(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Required>,
        onError: (ValidationError) -> Unit,
    ) {
        when {
            !areSessionNamespacesKeysProperlyFormatted(sessionNamespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areAllProposalNamespacesApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejected)
            !areAccountsNotEmpty(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_MISSING_MESSAGE))
            !areAccountIdsValid(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE))
            !areAccountsInMatchingNamespace(sessionNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE))
//            !areAllChainsApprovedWithAtLeastOneAccount(sessionNamespaces, proposalNamespaces) ->
//                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_MISSING_FOR_CHAINS_MESSAGE))
//            !areAllMethodsApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejectedMethods)
//            !areAllEventsApproved(sessionNamespaces, proposalNamespaces) -> onError(ValidationError.UserRejectedEvents)
//            !areExtensionAccountsNotEmpty(sessionNamespaces) ->
//                onError(ValidationError.UserRejectedChains(NAMESPACE_EXTENSION_ACCOUNTS_MISSING_MESSAGE))
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
        val maxExpiry = WEEK_IN_SECONDS

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
            topic = Topic(pairUri.userInfo),
            relay = RelayProtocolOptions(protocol = relayProtocol, data = relayData),
            symKey = SymmetricKey(symKey)
        )
    }

    private fun areProposalNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO.Required>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

//    private fun areChainsNotEmpty(namespaces: Map<String, NamespaceVO.Required>): Boolean =
//        namespaces.values.map { namespace -> namespace.chains }.all { chains -> chains.isNotEmpty() }

//    private fun areChainIdsValid(namespaces: Map<String, NamespaceVO.Required>): Boolean =
//        namespaces.values.flatMap { namespace -> namespace.chains }.all { chain -> isChainIdCAIP2Compliant(chain) }

//    private fun areChainsInMatchingNamespace(namespaces: Map<String, NamespaceVO.Required>): Boolean =
//        namespaces.all { (key, namespace) -> namespace.chains.all { chain -> chain.contains(key, true) } }

//    private fun areExtensionChainsNotEmpty(namespaces: Map<String, NamespaceVO.Proposal>): Boolean =
//        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.chains } }
//            .all { extChain -> extChain.isNotEmpty() }

    private fun areAllProposalNamespacesApproved(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        proposalNamespaces: Map<String, NamespaceVO.Required>,
    ): Boolean = sessionNamespaces.keys.containsAll(proposalNamespaces.keys)

    private fun areAccountsNotEmpty(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.map { namespace -> namespace.accounts }.all { accounts -> accounts.isNotEmpty() }

    private fun areAccountIdsValid(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.values.flatMap { namespace -> namespace.accounts }.all { accounts -> isAccountIdCAIP10Compliant(accounts) }

    private fun allApprovedMethodsWithChains(namespaces: Map<String, NamespaceVO.Session>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.methods.map { method -> method to namespace.accounts.map { getChainFromAccount(it) } }
        }.toMap()

//    private fun allRequiredMethodsWithChains(namespaces: Map<String, NamespaceVO.Required>): Map<String, List<String>> =
//        namespaces.values.flatMap { namespace -> namespace.methods.map { method -> method to namespace.chains } }.toMap()

//    private fun areAllMethodsApproved(
//        sessionNamespaces: Map<String, NamespaceVO.Session>,
//        proposalNamespaces: Map<String, NamespaceVO.Required>,
//    ): Boolean {
//        val allApprovedMethodsWithChains = allApprovedMethodsWithChains(sessionNamespaces)
//        val allRequiredMethodsWithChains = allRequiredMethodsWithChains(proposalNamespaces)
//
//        allRequiredMethodsWithChains.forEach { (method, chainsRequested) ->
//            val chainsApproved = allApprovedMethodsWithChains[method] ?: return false
//            if (!chainsApproved.containsAll(chainsRequested)) {
//                return false
//            }
//        }
//        return true
//    }

    private fun allApprovedEventsWithChains(namespaces: Map<String, NamespaceVO.Session>): Map<String, List<String>> =
        namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.accounts.map { getChainFromAccount(it) }
            }
        }.toMap()

//    private fun allRequiredEventsWithChains(namespaces: Map<String, NamespaceVO.Required>): Map<String, List<String>> =
//        namespaces.values.flatMap { namespace ->
//            namespace.events.map { event ->
//                event to namespace.chains
//            }
//        }.toMap()

//    private fun areAllEventsApproved(
//        sessionNamespaces: Map<String, NamespaceVO.Session>,
//        proposalNamespaces: Map<String, NamespaceVO.Required>,
//    ): Boolean {
//        val allApprovedEventsWithChains = allApprovedEventsWithChains(sessionNamespaces)
//        val allRequiredEventsWithChains = allRequiredEventsWithChains(proposalNamespaces)
//
//        allRequiredEventsWithChains.forEach { (method, chainsRequested) ->
//            val chainsApproved = allApprovedEventsWithChains[method] ?: return false
//            if (!chainsApproved.containsAll(chainsRequested)) {
//                return false
//            }
//        }
//        return true
//    }

//    private fun areAllChainsApprovedWithAtLeastOneAccount(
//        sessionNamespaces: Map<String, NamespaceVO.Session>,
//        proposalNamespaces: Map<String, NamespaceVO.Required>,
//    ): Boolean =
//        sessionNamespaces.values.flatMap { namespace -> namespace.accounts.map { it.substringBeforeLast(":") } }
//            .containsAll(proposalNamespaces.values.flatMap { namespace -> namespace.chains })

    private fun areAccountsInMatchingNamespace(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.all { (key, namespace) -> namespace.accounts.all { it.contains(key) } }

//    private fun areExtensionAccountsNotEmpty(namespaces: Map<String, NamespaceVO.Session>): Boolean =
//        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.accounts } }
//            .all { extAccount -> extAccount.isNotEmpty() }

    private fun areSessionNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO.Session>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

    @JvmSynthetic
    internal fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, _: String) = elements

        return "$namespace:$reference"
    }
}