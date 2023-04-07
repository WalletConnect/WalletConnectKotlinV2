package com.walletconnect.sign.client.utils

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toOptionalVO
import com.walletconnect.sign.client.mapper.toRequiredVO
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.validator.SignValidator

//normalize all namespaces
//check if all required are satisfied
//validate supporttednamespaces
//build sessionnamespaces

fun buildSessionNamespaces(proposal: Sign.Model.SessionProposal, supportedNamespaces: Map<String, Sign.Model.Namespace.Session>): Map<String, Sign.Model.Namespace.Session> {
    val optionalNamespaces = proposal.optionalNamespaces.toOptionalVO()
    val requiredNamespaces = proposal.requiredNamespaces.toRequiredVO()
    SignValidator.validateProposalNamespaces(optionalNamespaces) { error -> println("kobe; optional nm error: $error") /*todo: callback or throw*/ }
    SignValidator.validateProposalNamespaces(requiredNamespaces) { error -> println("kobe; required nm error: $error") /*todo: callback or throw*/ }


    return mapOf("" to Sign.Model.Namespace.Session(listOf(), listOf(), listOf(), listOf()))
}

internal fun normalizeNamespaces(namespaces: Map<String, NamespaceVO>): Map<String, NamespaceVO> {
    return mutableMapOf<String, NamespaceVO>().apply {
        namespaces.forEach { (key, namespace) ->
            if (SignValidator.)

        }
    }.toMap()
}


//private fun mergeRequiredAndOptional(required: Map<String, Wallet.Model.Namespace.Session>, optional: Map<String, Wallet.Model.Namespace.Session>) =
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
//
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