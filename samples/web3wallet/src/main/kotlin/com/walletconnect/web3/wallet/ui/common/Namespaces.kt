package com.walletconnect.web3.wallet.ui.common

import com.walletconnect.web3.wallet.client.Wallet

//todo change way of fetching chains
fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    return namespace.methods.takeIf { namespace.chains?.contains(chainId) == true } ?: emptyList()
}

//todo change way of fetching chains
fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Proposal, chainId: String): List<String> {
    return namespace.events.takeIf { namespace.chains!!.contains(chainId) } ?: emptyList()
}

fun getAllMethodsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    return namespace.methods.takeIf { namespace.accounts.contains(accountId) } ?: emptyList()
}

fun getAllEventsByChainId(namespace: Wallet.Model.Namespace.Session, accountId: String): List<String> {
    return namespace.events.takeIf { namespace.accounts.contains(accountId) } ?: emptyList()
}