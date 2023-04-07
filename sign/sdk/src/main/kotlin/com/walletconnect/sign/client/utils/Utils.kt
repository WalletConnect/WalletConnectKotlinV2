package com.walletconnect.sign.client.utils

import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toVO
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.validator.SignValidator

//check if all required are satisfied
//validate supporttednamespaces
//build sessionnamespaces from supported and required and optional

fun buildSessionNamespaces(proposal: Sign.Model.SessionProposal, supportedNamespaces: Map<String, Sign.Model.Namespace.Session>): Map<String, Sign.Model.Namespace.Session> {
    val optionalNamespaces = proposal.optionalNamespaces.toVO()
    val requiredNamespaces = proposal.requiredNamespaces.toVO()
    SignValidator.validateProposalNamespaces(optionalNamespaces) { error -> println("kobe; optional nm error: $error") /*todo: callback or throw*/ }
    SignValidator.validateProposalNamespaces(requiredNamespaces) { error -> println("kobe; required nm error: $error") /*todo: callback or throw*/ }
    val normalizedOptionalNamespaces = normalizeNamespaces(optionalNamespaces)
    val normalizedRequiredNamespaces = normalizeNamespaces(requiredNamespaces)


    return mapOf("" to Sign.Model.Namespace.Session(listOf(), listOf(), listOf(), listOf()))
}

internal fun normalizeNamespaces(namespaces: Map<String, NamespaceVO>): Map<String, NamespaceVO> {
    if (SignValidator.isNamespaceKeyRegexCompliant(namespaces)) return namespaces
    return mutableMapOf<String, NamespaceVO>().apply {
        namespaces.forEach { (key, namespace) ->
            val normalizedKey = normalizeKey(key)
            this[normalizedKey] = NamespaceVO.Proposal(
                chains = getChains(normalizedKey).merge(geNamespaceChains(key, namespace)),
                methods = getMethods(normalizedKey).merge(namespace.methods),
                events = getEvents(normalizedKey).merge(namespace.events)
            )
        }
    }.toMap()
}

private fun geNamespaceChains(key: String, namespace: NamespaceVO) = if (CoreValidator.isChainIdCAIP2Compliant(key)) listOf(key) else namespace.chains!!
private fun MutableMap<String, NamespaceVO>.getChains(normalizedKey: String) = (this[normalizedKey]?.chains ?: emptyList())
private fun MutableMap<String, NamespaceVO>.getMethods(normalizedKey: String) = (this[normalizedKey]?.methods ?: emptyList())
private fun MutableMap<String, NamespaceVO>.getEvents(normalizedKey: String) = (this[normalizedKey]?.events ?: emptyList())
private fun List<String>.merge(newList: List<String>) = this + newList
private fun normalizeKey(key: String): String = if (CoreValidator.isChainIdCAIP2Compliant(key)) SignValidator.getNamespaceKeyFromChainId(key) else key

//private fun mergeNamespaces(required: Map<String, NamespaceVO.Proposal>, optional: Map<String, NamespaceVO.Proposal>) =
//    (required.asSequence() + optional.asSequence())
//        .groupBy({ it.key }, { it.value })
//        .mapValues { entry ->
//            entry.value.reduce { acc, session ->
//                Wallet.Model.Namespace.Session(
//                    acc.chains?.plus(session.chains ?: emptyList()),
//                    acc.accounts.plus(session.accounts),
//                    acc.methods.plus(session.methods).distinct(),
//                    session.events.plus(session.events).distinct()
//                )
//            }
//        }

//private fun sessionNamespacesIndexedByChain(selectedAccounts: Map<Chains, String>, namespaces: Map<String, Wallet.Model.Namespace.Proposal>) =
//    selectedAccounts.filter { (chain: Chains, _) ->
//        namespaces
//            .filter { (namespaceKey, namespace) -> namespace.chains == null && namespaceKey == chain.chainId }
//            .isNotEmpty()
//    }.toList()
//        .groupBy { (chain: Chains, _: String) -> chain.chainId }
//        .asIterable()
//        .associate { (key: String, chainData: List<Pair<Chains, String>>) ->
//            val accounts = chainData.map { (chain: Chains, accountAddress: String) ->
//                "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
//            }
//
//            val methods = namespaces.values
//                .filter { namespace -> namespace.chains == null }
//                .flatMap { it.methods }
//
//            val events = namespaces.values
//                .filter { namespace -> namespace.chains == null }
//                .flatMap { it.events }
//
//            key to Wallet.Model.Namespace.Session(
//                accounts = accounts,
//                methods = methods,
//                events = events
//            )
//        }
//
//private fun getSessionNamespacesIndexedByNamespace(selectedAccounts: Map<Chains, String>, namespaces: Map<String, Wallet.Model.Namespace.Proposal>, supportedChains: List<String>) =
//    selectedAccounts.filter { (chain: Chains, _) ->
//        namespaces
//            .filter { (_, namespace) -> namespace.chains != null }
//            .flatMap { (_, namespace) -> namespace.chains!! }
//            .contains(chain.chainId)
//    }.toList()
//        .groupBy { (chain: Chains, _: String) -> chain.chainNamespace }
//        .asIterable()
//        .associate { (key: String, chainData: List<Pair<Chains, String>>) ->
//
//            val accounts = chainData.map { (chain: Chains, accountAddress: String) ->
//                "${chain.chainNamespace}:${chain.chainReference}:${accountAddress}"
//            }
//
//            val methods = namespaces.values
//                .filter { namespace -> namespace.chains != null }
//                .flatMap { it.methods }
//
//            val events = namespaces.values
//                .filter { namespace -> namespace.chains != null }
//                .flatMap { it.events }
//
//            val chains: List<String> =
//                namespaces.values
//                    .filter { namespace -> namespace.chains != null }
//                    .flatMap { namespace ->
//                        mutableListOf<String>().apply {
//                            namespace.chains!!.forEach { chain ->
//                                if (supportedChains.contains(chain)) {
//                                    add(chain)
//                                }
//                            }
//                        }
//                    }
//
//            key to Wallet.Model.Namespace.Session(
//                accounts = accounts,
//                methods = methods,
//                events = events,
//                chains = chains.ifEmpty { null })
//        }