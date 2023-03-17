@file:JvmSynthetic

package com.walletconnect.sign.engine.domain

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.utils.CoreValidator.NAMESPACE_REGEX
import com.walletconnect.android.internal.utils.CoreValidator.isAccountIdCAIP10Compliant
import com.walletconnect.android.internal.utils.CoreValidator.isChainIdCAIP2Compliant
import com.walletconnect.android.internal.utils.WEEK_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.*
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.ValidationError
import java.net.URI
import java.net.URISyntaxException

internal object SignValidator {

    @JvmSynthetic
    internal inline fun validateProposalNamespaces(namespaces: Map<String, NamespaceVO>, onError: (ValidationError) -> Unit) {
        when {
            !areNamespacesKeysProperlyFormatted(namespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areChainsDefined(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_UNDEFINED_MISSING_MESSAGE))
            !areChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_MISSING_MESSAGE))
            !areChainIdsValid(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_CAIP_2_MESSAGE))
            !areChainsInMatchingNamespace(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE))
        }
    }

    @JvmSynthetic
    internal inline fun validateSessionNamespace(
        sessionNamespaces: Map<String, NamespaceVO.Session>,
        requiredNamespaces: Map<String, NamespaceVO.Required>,
        onError: (ValidationError) -> Unit,
    ) {
        when {
            sessionNamespaces.isEmpty() -> onError(ValidationError.EmptyNamespaces)
            !areNamespacesKeysProperlyFormatted(sessionNamespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areChainsNotEmpty(sessionNamespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_MISSING_MESSAGE))
            !areChainIdsValid(sessionNamespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_CAIP_2_MESSAGE))
            !areChainsInMatchingNamespace(sessionNamespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE))
            !areAccountIdsValid(sessionNamespaces) -> onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_CAIP_10_MESSAGE))
            !areAccountsInMatchingNamespace(sessionNamespaces) ->
                onError(ValidationError.UserRejectedChains(NAMESPACE_ACCOUNTS_WRONG_NAMESPACE_MESSAGE))
            !areAllNamespacesApproved(sessionNamespaces.keys, requiredNamespaces.keys) -> onError(ValidationError.UserRejected)
            !areAllMethodsApproved(allMethodsWithChains(sessionNamespaces), allMethodsWithChains(requiredNamespaces)) ->
                onError(ValidationError.UserRejectedMethods)
            !areAllEventsApproved(allEventsWithChains(sessionNamespaces), allEventsWithChains(requiredNamespaces)) ->
                onError(ValidationError.UserRejectedEvents)
        }
    }

    @JvmSynthetic
    internal inline fun validateProperties(properties: Map<String, String>, onError: (ValidationError) -> Unit) {
        if (properties.isEmpty()) {
            onError(ValidationError.InvalidSessionProperties)
        }
    }

    private fun areAllNamespacesApproved(sessionNamespacesKeys: Set<String>, proposalNamespacesKeys: Set<String>): Boolean =
        sessionNamespacesKeys.containsAll(proposalNamespacesKeys)

    @JvmSynthetic
    internal inline fun validateChainIdWithMethodAuthorisation(
        chainId: String,
        method: String,
        namespaces: Map<String, NamespaceVO.Session>,
        onError: (ValidationError) -> Unit,
    ) {
        allMethodsWithChains(namespaces).also { allApprovedMethodsWithChains ->
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
        allEventsWithChains(namespaces).also { allApprovedMethodsWithChains ->
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

    private fun allMethodsWithChains(namespaces: Map<String, NamespaceVO>): Map<String, List<String>> {
        val methodsByChains = namespaces
            .filter { (namespaceKey, namespace) -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains != null }
            .flatMap { (_, namespace) -> namespace.methods.map { method -> method to namespace.chains!! } }
            .toMap()

        val methodsByNamespaceKey = namespaces
            .filter { (namespaceKey, namespace) -> isChainIdCAIP2Compliant(namespaceKey) && namespace.chains == null }
            .flatMap { (namespaceKey, namespace) -> namespace.methods.map { method -> method to listOf(namespaceKey) } }
            .toMap()

        val methodsByChainFromAccount = namespaces
            .filter { (namespaceKey, namespace) -> namespace is NamespaceVO.Session && NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains == null }
            .flatMap { (_, namespace) -> (namespace as NamespaceVO.Session).methods.map { method -> method to namespace.accounts.map { getChainFromAccount(it) } } }
            .toMap()

        return (methodsByChains.asSequence() + methodsByNamespaceKey.asSequence() + methodsByChainFromAccount.asSequence())
            .distinct()
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.flatten() }
    }

    private fun areAllMethodsApproved(
        allApprovedMethodsWithChains: Map<String, List<String>>,
        allRequiredMethodsWithChains: Map<String, List<String>>,
    ): Boolean {
        allRequiredMethodsWithChains.forEach { (method, chainsRequested) ->
            val chainsApproved = allApprovedMethodsWithChains[method] ?: return false
            if (!chainsApproved.containsAll(chainsRequested)) {
                return false
            }
        }
        return true
    }

    private fun allEventsWithChains(namespaces: Map<String, NamespaceVO>): Map<String, List<String>> {
        val eventsByChains = namespaces
            .filter { (namespaceKey, namespace) -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains != null }
            .flatMap { (_, namespace) -> namespace.events.map { event -> event to namespace.chains!! } }
            .toMap()

        val eventsByNamespaceKey = namespaces
            .filter { (namespaceKey, namespace) -> isChainIdCAIP2Compliant(namespaceKey) && namespace.chains == null }
            .flatMap { (namespaceKey, namespace) -> namespace.events.map { event -> event to listOf(namespaceKey) } }
            .toMap()

        val eventsByChainFromAccount = namespaces
            .filter { (namespaceKey, namespace) -> namespace is NamespaceVO.Session && NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains == null }
            .flatMap { (_, namespace) -> (namespace as NamespaceVO.Session).events.map { event -> event to namespace.accounts.map { getChainFromAccount(it) } } }
            .toMap()

        return (eventsByChains.asSequence() + eventsByNamespaceKey.asSequence() + eventsByChainFromAccount.asSequence())
            .distinct()
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.flatten() }
    }

    private fun areAllEventsApproved(
        allApprovedEventsWithChains: Map<String, List<String>>,
        allRequiredEventsWithChains: Map<String, List<String>>
    ): Boolean {
        allRequiredEventsWithChains.forEach { (method, chainsRequested) ->
            val chainsApproved = allApprovedEventsWithChains[method] ?: return false
            if (!chainsApproved.containsAll(chainsRequested)) {
                return false
            }
        }
        return true
    }

    private fun areNamespacesKeysProperlyFormatted(namespaces: Map<String, NamespaceVO>): Boolean =
        namespaces.all { (namespaceKey, _) -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) || isChainIdCAIP2Compliant(namespaceKey) }

    private fun areAccountIdsValid(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.all { (_, namespace) -> namespace.accounts.all { accountId -> isAccountIdCAIP10Compliant(accountId) } }

    private fun areAccountsInMatchingNamespace(sessionNamespaces: Map<String, NamespaceVO.Session>): Boolean =
        sessionNamespaces.all { (namespaceKey, namespace) ->
            if (NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains != null) {
                namespace.accounts.all { accountId ->
                    accountId.contains(namespaceKey) && namespace.chains.contains(getChainFromAccount(accountId))
                }
            } else {
                namespace.accounts.all { accountId ->
                    accountId.contains(namespaceKey)
                }
            }
        }

    private fun areChainsDefined(namespaces: Map<String, NamespaceVO>): Boolean =
        namespaces.filter { (namespaceKey, namespace) ->
            NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains == null
        }.isEmpty()

    private fun areChainsNotEmpty(namespaces: Map<String, NamespaceVO>): Boolean =
        namespaces.filter { (namespaceKey, namespace) ->
            NAMESPACE_REGEX.toRegex().matches(namespaceKey) && namespace.chains != null && namespace.chains!!.isEmpty()
        }.isEmpty()

    private fun areChainIdsValid(namespaces: Map<String, NamespaceVO>): Boolean =
        getValidNamespaces(namespaces).flatMap { (_, namespace) -> namespace.chains!! }.all { chain -> isChainIdCAIP2Compliant(chain) }

    private fun areChainsInMatchingNamespace(namespaces: Map<String, NamespaceVO>): Boolean =
        getValidNamespaces(namespaces)
            .all { (namespaceKey, namespace) -> namespace.chains!!.all { chain -> chain.contains(namespaceKey, true) } }

    private fun getValidNamespaces(namespaces: Map<String, NamespaceVO>) =
        namespaces.filter { (_, namespace) -> namespace.chains != null && namespace.chains!!.isNotEmpty() }

    @JvmSynthetic
    internal fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, _: String) = elements

        return "$namespace:$reference"
    }
}