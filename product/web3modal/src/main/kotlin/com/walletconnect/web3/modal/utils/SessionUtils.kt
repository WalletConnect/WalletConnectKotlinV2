package com.walletconnect.web3.modal.utils

import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.model.Chain

internal fun String.toVisibleAddress() = "${take(4)}...${takeLast(4)}"

internal fun List<Chain>.getSelectedChain(chainId: String?) = find { it.id == chainId } ?: first()

internal fun Modal.Model.Session.getAddress(selectedChain: Chain) = getAccounts().find { it.startsWith(selectedChain.id) }?.split(":")?.last() ?: String.Empty

internal fun Modal.Model.Session.getChains() = namespaces.values.toList()
    .flatMap { it.chains ?: listOf() }
    .filter { CoreValidator.isChainIdCAIP2Compliant(it) }
    .map { Chain(it) }
    .ifEmpty { getDefaultChain() }

private fun Modal.Model.Session.getAccounts() = namespaces.values.toList().flatMap { it.accounts }

private fun Modal.Model.Session.getDefaultChain() = getAccounts()
    .accountsToChainId()
    .filter { CoreValidator.isChainIdCAIP2Compliant(it) }
    .map { Chain(it) }

private fun List<String>.accountsToChainId() = map {
    val (chainNamespace, chainReference, _) = it.split(":")
    "$chainNamespace:$chainReference"
}
