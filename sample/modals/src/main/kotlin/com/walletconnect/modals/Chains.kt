package com.walletconnect.modals

import com.walletconnect.sample.common.Chains
import com.walletconnect.web3.modal.client.Modal

fun modalChains() = listOf(
    ethereumChain,
    arbitrumOneChain,
    polygonChain,
    avalancheChain,
    bnbSmartChain,
    opMainnetChain,
    gnosisChain,
    zkSyncEraChain,
    zoraChain,
    baseChain,
    celoChain,
    auroraChain
)

private val auroraChain = Modal.Model.Chain(
    chainName = "Aurora",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "1313161554",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val arbitrumOneChain = Modal.Model.Chain(
    chainName = "Arbitrum One",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "42161",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val avalancheChain = Modal.Model.Chain(
    chainName = "Avalanche",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "43114",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val baseChain = Modal.Model.Chain(
    chainName = "Base",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "8453",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val bnbSmartChain = Modal.Model.Chain(
    chainName = "BNB Smart Chain",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "56",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val celoChain = Modal.Model.Chain(
    chainName = "Celo",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "42220",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val ethereumChain = Modal.Model.Chain(
    chainName = "Ethereum",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "1",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val gnosisChain = Modal.Model.Chain(
    chainName = "Gnosis",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "100",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val opMainnetChain = Modal.Model.Chain(
    chainName = "OP Mainnet",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "10",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val polygonChain = Modal.Model.Chain(
    chainName = "Polygon",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "137",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val zkSyncEraChain = Modal.Model.Chain(
    chainName = "zkSync Era",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "324",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)

private val zoraChain = Modal.Model.Chain(
    chainName = "Zora",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "7777777",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents
)
