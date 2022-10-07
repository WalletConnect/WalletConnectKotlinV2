package com.walletconnect.android.internal

import com.walletconnect.android.common.model.RelayProtocolOptions
import com.walletconnect.android.common.model.SymmetricKey
import com.walletconnect.android.common.model.WalletConnectUri
import com.walletconnect.foundation.common.model.Topic
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    @JvmSynthetic
    internal fun validateWCUri(uri: String): WalletConnectUri? {
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

        return WalletConnectUri(
            topic = Topic(pairUri.userInfo),
            relay = RelayProtocolOptions(protocol = relayProtocol, data = relayData),
            symKey = SymmetricKey(symKey)
        )
    }

    @JvmSynthetic
    internal inline fun validateProposalNamespace(namespaces: Map<String, Proposal>, onError: (ValidationError) -> Unit) {
        when {
            !areProposalNamespacesKeysProperlyFormatted(namespaces) -> onError(ValidationError.UnsupportedNamespaceKey)
            !areChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_MISSING_MESSAGE))
            !areChainIdsValid(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_CAIP_2_MESSAGE))
            !areChainsInMatchingNamespace(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_CHAINS_WRONG_NAMESPACE_MESSAGE))
            !areExtensionChainsNotEmpty(namespaces) -> onError(ValidationError.UnsupportedChains(NAMESPACE_EXTENSION_CHAINS_MISSING_MESSAGE))
        }
    }

    private fun areProposalNamespacesKeysProperlyFormatted(namespaces: Map<String, Proposal>): Boolean =
        namespaces.keys.all { namespaceKey -> NAMESPACE_REGEX.toRegex().matches(namespaceKey) }

    private fun areChainsNotEmpty(namespaces: Map<String, Proposal>): Boolean =
        namespaces.values.map { namespace -> namespace.chains }.all { chains -> chains.isNotEmpty() }

    private fun areChainIdsValid(namespaces: Map<String, Proposal>): Boolean =
        namespaces.values.flatMap { namespace -> namespace.chains }.all { chain -> isChainIdCAIP2Compliant(chain) }

    private fun areChainsInMatchingNamespace(namespaces: Map<String, Proposal>): Boolean =
        namespaces.all { (key, namespace) -> namespace.chains.all { chain -> chain.contains(key, true) } }

    private fun areExtensionChainsNotEmpty(namespaces: Map<String, Proposal>): Boolean =
        namespaces.values.filter { it.extensions != null }.flatMap { namespace -> namespace.extensions!!.map { it.chains } }
            .all { extChain -> extChain.isNotEmpty() }
}