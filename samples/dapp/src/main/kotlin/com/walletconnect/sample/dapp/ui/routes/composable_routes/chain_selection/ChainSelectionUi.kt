package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import com.walletconnect.sample.dapp.domain.model.Chain

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

fun Chain.toChainUiState() = ChainSelectionUi(chainName, chainNamespace, chainReference, icon, color, methods, events)
