package com.walletconnect.dapp.ui.connect.chain_select

data class ChainSelectionUI(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: Int,
    val icon: Int,
    val methods: List<String>,
    val events: List<String>,
    var isSelected: Boolean = false,
) {
    val chainId = "${chainNamespace}:${chainReference}"
}