@file:JvmName("ApprovedNamespacesUtilsKt")
package com.walletconnect.sign.client.utils

import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toClient
import com.walletconnect.sign.client.mapper.toProposalNamespacesVO
import com.walletconnect.sign.client.mapper.toSessionNamespacesVO
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
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

    val approvedNamespaces = mutableMapOf<String, NamespaceVO.Session>()
    normalizedRequiredNamespaces.forEach { (key, requiredNamespace) ->
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> requiredNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> requiredNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> requiredNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = NamespaceVO.Session(chains = chains, methods = methods, events = events, accounts = accounts)
    }

    normalizedOptionalNamespaces.forEach { (key, optionalNamespace) ->
        if (!supportedNamespaces.containsKey(key)) return@forEach
        val chains = supportedNamespacesVO[key]?.chains?.filter { chain -> optionalNamespace.chains!!.contains(chain) } ?: emptyList()
        val methods = supportedNamespaces[key]?.methods?.filter { method -> optionalNamespace.methods.contains(method) } ?: emptyList()
        val events = supportedNamespaces[key]?.events?.filter { event -> optionalNamespace.events.contains(event) } ?: emptyList()
        val accounts = chains.flatMap { chain -> supportedNamespaces[key]?.accounts?.filter { account -> SignValidator.getChainFromAccount(account) == chain } ?: emptyList() }

        approvedNamespaces[key] = NamespaceVO.Session(
            chains = approvedNamespaces[key]?.chains?.plus(chains)?.distinct(),
            methods = approvedNamespaces[key]?.methods?.plus(methods)?.distinct() ?: emptyList(),
            events = approvedNamespaces[key]?.events?.plus(events)?.distinct() ?: emptyList(),
            accounts = approvedNamespaces[key]?.accounts?.plus(accounts)?.distinct() ?: emptyList()
        )
    }

    return approvedNamespaces.toClient()
}

internal fun normalizeNamespaces(namespaces: Map<String, NamespaceVO.Proposal>): Map<String, NamespaceVO.Proposal> {
    if (SignValidator.isNamespaceKeyRegexCompliant(namespaces)) return namespaces
    return mutableMapOf<String, NamespaceVO.Proposal>().apply {
        namespaces.forEach { (key, namespace) ->
            val normalizedKey = normalizeKey(key)
            this[normalizedKey] = NamespaceVO.Proposal(
                chains = getChains(normalizedKey).plus(getNamespaceChains(key, namespace)),
                methods = getMethods(normalizedKey).plus(namespace.methods),
                events = getEvents(normalizedKey).plus(namespace.events)
            )
        }
    }.toMap()
}

private fun getNamespaceChains(key: String, namespace: NamespaceVO) = if (CoreValidator.isChainIdCAIP2Compliant(key)) listOf(key) else namespace.chains!!
private fun normalizeKey(key: String): String = if (CoreValidator.isChainIdCAIP2Compliant(key)) SignValidator.getNamespaceKeyFromChainId(key) else key
private fun MutableMap<String, NamespaceVO.Proposal>.getChains(normalizedKey: String) = (this[normalizedKey]?.chains ?: emptyList())
private fun MutableMap<String, NamespaceVO.Proposal>.getMethods(normalizedKey: String) = (this[normalizedKey]?.methods ?: emptyList())
private fun MutableMap<String, NamespaceVO.Proposal>.getEvents(normalizedKey: String) = (this[normalizedKey]?.events ?: emptyList())