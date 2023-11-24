@file:JvmName("ApprovedNamespacesUtils")

package com.walletconnect.sign.client.utils

import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toClient
import com.walletconnect.sign.client.mapper.toProposalNamespacesVO
import com.walletconnect.sign.client.mapper.toSessionNamespacesVO
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.sign.common.validator.SignValidator

fun generateApprovedNamespaces(
    proposal: Sign.Model.SessionProposal,
    supportedNamespaces: Map<String, Sign.Model.Namespace.Session>,
): Map<String, Sign.Model.Namespace.Session> {
    val supportedNamespacesVO = supportedNamespaces.toSessionNamespacesVO()
    val normalizedRequiredNamespaces = normalizeNamespaces(proposal.requiredNamespaces.toProposalNamespacesVO())
    val normalizedOptionalNamespaces = normalizeNamespaces(proposal.optionalNamespaces.toProposalNamespacesVO())

    SignValidator.validateProposalNamespaces(normalizedRequiredNamespaces) { error -> throw Exception(error.message) }
    SignValidator.validateProposalNamespaces(normalizedOptionalNamespaces) { error -> throw Exception(error.message) }
    SignValidator.validateSupportedNamespace(supportedNamespacesVO, normalizedRequiredNamespaces) { error -> throw Exception(error.message) }

    if (proposal.requiredNamespaces.isEmpty() && proposal.optionalNamespaces.isEmpty()) {
        return supportedNamespacesVO.toClient()
    }

    val approvedNamespaces = mutableMapOf<String, Namespace.Session>()
    normalizedRequiredNamespaces.forEach { (key, requiredNamespace) ->
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> requiredNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> requiredNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> requiredNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = Namespace.Session(chains = chains, methods = methods, events = events, accounts = accounts)
    }

    normalizedOptionalNamespaces.forEach { (key, optionalNamespace) ->
        if (!supportedNamespaces.containsKey(key)) return@forEach
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> optionalNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> optionalNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> optionalNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = Namespace.Session(
            chains = approvedNamespaces[key]?.chains?.plus(chains)?.distinct() ?: chains,
            methods = approvedNamespaces[key]?.methods?.plus(methods)?.distinct() ?: methods,
            events = approvedNamespaces[key]?.events?.plus(events)?.distinct() ?: events,
            accounts = approvedNamespaces[key]?.accounts?.plus(accounts)?.distinct() ?: accounts
        )
    }

    return approvedNamespaces.toClient()
}

internal fun normalizeNamespaces(namespaces: Map<String, Namespace.Proposal>): Map<String, Namespace.Proposal> {
    if (SignValidator.isNamespaceKeyRegexCompliant(namespaces)) return namespaces
    return mutableMapOf<String, Namespace.Proposal>().apply {
        namespaces.forEach { (key, namespace) ->
            val normalizedKey = normalizeKey(key)
            this[normalizedKey] = Namespace.Proposal(
                chains = getChains(normalizedKey).plus(getNamespaceChains(key, namespace)),
                methods = getMethods(normalizedKey).plus(namespace.methods),
                events = getEvents(normalizedKey).plus(namespace.events)
            )
        }
    }.toMap()
}

private fun getNamespaceChains(key: String, namespace: Namespace) = if (CoreValidator.isChainIdCAIP2Compliant(key)) listOf(key) else namespace.chains!!
private fun normalizeKey(key: String): String = if (CoreValidator.isChainIdCAIP2Compliant(key)) SignValidator.getNamespaceKeyFromChainId(key) else key
private fun MutableMap<String, Namespace.Proposal>.getChains(normalizedKey: String) = (this[normalizedKey]?.chains ?: emptyList())
private fun MutableMap<String, Namespace.Proposal>.getMethods(normalizedKey: String) = (this[normalizedKey]?.methods ?: emptyList())
private fun MutableMap<String, Namespace.Proposal>.getEvents(normalizedKey: String) = (this[normalizedKey]?.events ?: emptyList())