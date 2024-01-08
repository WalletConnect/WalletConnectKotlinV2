package com.walletconnect.web3.modal.utils

import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.client.models.Account
import com.walletconnect.web3.modal.domain.model.Session

internal fun String.toVisibleAddress() = "${take(4)}...${takeLast(4)}"

internal fun List<Modal.Model.Chain>.getSelectedChain(chainId: String?) = find { it.id == chainId } ?: first()

internal fun Modal.Model.Session.getAddress(selectedChain: Modal.Model.Chain) = getAccounts().find { it.startsWith(selectedChain.id) }?.split(":")?.last() ?: String.Empty

internal fun Modal.Model.Session.getChains() = namespaces.values.toList()
    .flatMap { it.chains ?: listOf() }
    .filter { CoreValidator.isChainIdCAIP2Compliant(it) }
    .mapNotNull { it.toChain() }
    .ifEmpty { getDefaultChain() }

internal fun Modal.Model.UpdatedSession.getChains() = namespaces.values.toList()
    .flatMap { it.chains ?: listOf() }
    .mapNotNull { it.toChain() }

internal fun Modal.Model.UpdatedSession.getAddress(selectedChain: Modal.Model.Chain) = namespaces.values.toList().flatMap { it.accounts }.find { it.startsWith(selectedChain.id) }?.split(":")?.last() ?: String.Empty
internal fun Modal.Model.UpdatedSession.toSession(selectedChain: Modal.Model.Chain) = Session.WalletConnect(getAddress(selectedChain), selectedChain.id, topic)

internal fun String.toChain() = Web3Modal.chains.find { it.id == this }

private fun Modal.Model.Session.getAccounts() = namespaces.values.toList().flatMap { it.accounts }

private fun Modal.Model.Session.getDefaultChain() = getAccounts()
    .accountsToChainId()
    .filter { CoreValidator.isChainIdCAIP2Compliant(it) }
    .mapNotNull { it.toChain() }

private fun List<String>.accountsToChainId() = map {
    val (chainNamespace, chainReference, _) = it.split(":")
    "$chainNamespace:$chainReference"
}

internal fun Modal.Model.ApprovedSession.WalletConnectSession.getAddress(chain: Modal.Model.Chain) = namespaces.values.toList()
    .flatMap { it.accounts }
    .find { it.startsWith(chain.id) }
    ?.split(":")
    ?.last() ?: String.Empty

internal fun Session.getChains() = when(this) {
    is Session.Coinbase -> Web3Modal.chains.filter { it.id == this.chain }
    is Session.WalletConnect -> Web3Modal.getActiveSessionByTopic(topic)?.getChains() ?: Web3Modal.chains
}

internal fun Session.toAccount() = Account(address, getChain(chain))

internal fun getChain(chainId: String) = Web3Modal.chains.find { it.id == chainId } ?: Web3Modal.chains.first()

internal fun Session.toConnectorType() = when(this) {
    is Session.Coinbase -> Modal.ConnectorType.WALLET_CONNECT
    is Session.WalletConnect -> Modal.ConnectorType.COINBASE
}
