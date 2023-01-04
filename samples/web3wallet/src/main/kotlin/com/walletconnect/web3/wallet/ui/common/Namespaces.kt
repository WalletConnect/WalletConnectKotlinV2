package com.walletconnect.web3.wallet.ui.common

import com.walletconnect.web3.wallet.client.Wallet

fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.chains.map { chain ->
        chain to namespace.methods
    }.plus(
        namespace.extensions?.flatMap { extension ->
            extension.chains.map { chain ->
                chain to extension.methods
            }
        } ?: emptyList()
    ).onEach { (chain, methods) ->
        map[chain] = (map[chain] ?: emptyList()).plus(methods)
    }

    return map[chainId] ?: emptyList()
}

fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.chains.map { chain ->
        chain to namespace.events
    }.plus(
        namespace.extensions?.flatMap { extension ->
            extension.chains.map { chain ->
                chain to extension.events
            }
        } ?: emptyList()
    ).onEach { (chain, events) ->
        map[chain] = (map[chain] ?: emptyList()).plus(events)
    }

    return map[chainId] ?: emptyList()
}


fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.accounts.map { account ->
        account to namespace.methods
    }.plus(
        namespace.extensions?.flatMap { extension ->
            extension.accounts.map { account ->
                account to extension.methods
            }
        } ?: emptyList()
    ).onEach { (account, methods) ->
        map[account] = (map[account] ?: emptyList()).plus(methods)
    }

    return map[accountId] ?: emptyList()
}

fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    val map = mutableMapOf<String, List<String>>()
    namespace.accounts.map { account ->
        account to namespace.events
    }.plus(
        namespace.extensions?.flatMap { extension ->
            extension.accounts.map { account ->
                account to extension.events
            }
        } ?: emptyList()
    ).onEach { (account, events) ->
        map[account] = (map[account] ?: emptyList()).plus(events)
    }

    return map[accountId] ?: emptyList()
}