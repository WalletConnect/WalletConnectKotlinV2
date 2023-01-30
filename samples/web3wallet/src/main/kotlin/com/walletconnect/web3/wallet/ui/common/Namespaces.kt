package com.walletconnect.web3.wallet.ui.common

import com.walletconnect.web3.wallet.client.Wallet

fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.chains
        .map { chain -> chain to namespace.methods }
        .onEach { (chain, methods) -> map[chain] = (map[chain] ?: emptyList()).plus(methods) }

    return map[chainId] ?: emptyList()
}

fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.chains
        .map { chain -> chain to namespace.events }
        .onEach { (chain, events) -> map[chain] = (map[chain] ?: emptyList()).plus(events) }

    return map[chainId] ?: emptyList()
}

fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.accounts
        .map { account -> account to namespace.methods }
        .onEach { (account, methods) -> map[account] = (map[account] ?: emptyList()).plus(methods) }

    return map[accountId] ?: emptyList()
}

fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.accounts
        .map { account -> account to namespace.events }
        .onEach { (account, events) -> map[account] = (map[account] ?: emptyList()).plus(events) }

    return map[accountId] ?: emptyList()
}