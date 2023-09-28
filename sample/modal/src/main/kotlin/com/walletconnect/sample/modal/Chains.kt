package com.walletconnect.sample.modal

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

private val ethToken = Modal.Model.Token(
    name = "Ether",
    symbol = "ETH",
    decimal = 18,
)

private val auroraChain = Modal.Model.Chain(
    chainName = "Aurora",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "1313161554",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://mainnet.aurora.dev",
    blockExplorerUrl = "https://aurorascan.dev"
)

private val arbitrumOneChain = Modal.Model.Chain(
    chainName = "Arbitrum One",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "42161",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://arb1.arbitrum.io/rpc",
    blockExplorerUrl = "https://arbiscan.io"
)

private val avalancheChain = Modal.Model.Chain(
    chainName = "Avalanche",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "43114",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://api.avax.network/ext/bc/C/rpc",
    blockExplorerUrl = "https://snowtrace.io"
)

private val baseChain = Modal.Model.Chain(
    chainName = "Base",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "8453",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://mainnet.base.org",
    blockExplorerUrl = "https://basescan.org"
)

private val bnbSmartChain = Modal.Model.Chain(
    chainName = "BNB Smart Chain",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "56",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = Modal.Model.Token("BNB", "BNB", 18),
    rpcUrl = "https://rpc.ankr.com/bsc",
    blockExplorerUrl = "https://bscscan.com"
)

private val celoChain = Modal.Model.Chain(
    chainName = "Celo",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "42220",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = Modal.Model.Token("CELO", "CELO", 18),
    rpcUrl = "https://forno.celo.org",
    blockExplorerUrl = "https://explorer.celo.org/mainnet"
)

private val ethereumChain = Modal.Model.Chain(
    chainName = "Ethereum",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "1",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://cloudflare-eth.com",
    blockExplorerUrl = "https://etherscan.io"
)

private val gnosisChain = Modal.Model.Chain(
    chainName = "Gnosis",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "100",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = Modal.Model.Token("Gnosis", "xDAI", 18),
    rpcUrl = "https://rpc.gnosischain.com",
    blockExplorerUrl = "https://blockscout.com/xdai/mainnet"
)

private val opMainnetChain = Modal.Model.Chain(
    chainName = "OP Mainnet",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "10",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://mainnet.optimism.io",
    blockExplorerUrl = "https://explorer.optimism.io"
)

private val polygonChain = Modal.Model.Chain(
    chainName = "Polygon",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "137",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = Modal.Model.Token("MATIC", "MATIC", 18),
    rpcUrl = "https://polygon-rpc.com",
    blockExplorerUrl = "https://polygonscan.com"
)

private val zkSyncEraChain = Modal.Model.Chain(
    chainName = "zkSync Era",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "324",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://mainnet.era.zksync.io",
    blockExplorerUrl = "https://explorer.zksync.io"
)

private val zoraChain = Modal.Model.Chain(
    chainName = "Zora",
    chainNamespace = Chains.Info.Eth.chain,
    chainReference = "7777777",
    methods = Chains.Info.Eth.defaultMethods,
    events = Chains.Info.Eth.defaultEvents,
    token = ethToken,
    rpcUrl = "https://rpc.zora.energy",
    blockExplorerUrl = "https://explorer.zora.energy"
)
