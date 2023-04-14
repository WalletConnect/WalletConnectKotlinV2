package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import com.walletconnect.sample_common.Chains

data class ChainSelectionUi(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: String,
    val icon: Int,
    val color: String,
    val methods: List<String>,
    val events: List<String>,
    var isSelected: Boolean = false,
) {
    val chainId = "${chainNamespace}:${chainReference}"
}

fun Chains.toChainUiState() = ChainSelectionUi(chainName, chainNamespace, chainReference, icon, color, methods, events)
