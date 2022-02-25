package com.walletconnect.dapp.ui.chain_select

data class ChainSelectionUI(
    val chainName: String,
    val parentChain: String,
    val chainId: Int,
    val icon: Int,
    val methods: List<String>,
    var isSelected: Boolean = false,
)