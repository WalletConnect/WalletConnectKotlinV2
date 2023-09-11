package com.walletconnect.web3.modal.domain.model

internal data class AccountData(
    val address: String,
    val topic: String,
    val balance: String,
    val selectedChain: Chain,
    val chains: List<Chain>
)
