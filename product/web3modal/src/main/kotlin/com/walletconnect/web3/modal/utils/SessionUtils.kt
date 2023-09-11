package com.walletconnect.web3.modal.utils

import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.domain.model.Chain

internal fun String.toVisibleAddress() = "${take(4)}...${takeLast(4)}"

internal fun List<Chain>.getSelectedChain(chainId: String?) = find { it.id == chainId }?: first()
internal fun List<String>.getAddress(selectedChain: Chain) = find { it.startsWith(selectedChain.id) }?.split(":")?.last() ?: String.Empty

