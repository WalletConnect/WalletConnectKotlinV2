package com.walletconnect.sample.dapp.domain.model

import com.walletconnect.sample_common.Chains

data class Chain(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: String,
    val icon: Int,
    val color: String,
    val methods: List<String>,
    val events: List<String>,
    val order: Int,
)

fun Chains.toChain() =
    Chain(chainName, chainNamespace, chainReference, icon, color, methods, events, order)
