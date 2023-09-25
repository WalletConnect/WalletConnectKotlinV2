package com.walletconnect.web3.modal.utils

import com.walletconnect.web3.modal.client.Modal

internal fun Modal.Model.ChainImage.getImageData() = when(this) {
    is Modal.Model.ChainImage.Asset -> id
    is Modal.Model.ChainImage.Network -> url
}
internal fun Modal.Model.Chain.getImageData(): Any = when(chainImage) {
    is Modal.Model.ChainImage.Asset -> chainImage.id
    is Modal.Model.ChainImage.Network -> chainImage.url
    null -> getChainNetworkImageUrl(chainReference).url
}

internal fun getChainNetworkImageUrl(chainReference: String) = Modal.Model.ChainImage.Network("https://api.web3modal.com/public/getAssetImage/${networkImagesIds[chainReference]}")

internal val networkImagesIds = mapOf(
    // Ethereum
    "1" to "692ed6ba-e569-459a-556a-776476829e00",
    // Ethereum Kovan
    "42" to "692ed6ba-e569-459a-556a-776476829e00",
    // Arbitrum
    "42161" to "600a9a04-c1b9-42ca-6785-9b4b6ff85200",
    // Avalanche
    "43114" to "30c46e53-e989-45fb-4549-be3bd4eb3b00",
    // Binance Smart Chain
    "56" to "93564157-2e8e-4ce7-81df-b264dbee9b00",
    // Fantom
    "250" to "06b26297-fe0c-4733-5d6b-ffa5498aac00",
    // Optimism
    "10" to "ab9c186a-c52f-464b-2906-ca59d760a400",
    // Optimism Kovan
    "69" to "ab9c186a-c52f-464b-2906-ca59d760a400",
    // Polygon
    "137" to "41d04d42-da3b-4453-8506-668cc0727900",
    // Polygon Mumbai
    "80001" to "41d04d42-da3b-4453-8506-668cc0727900",
    // Gnosis
    "100" to "02b53f6a-e3d4-479e-1cb4-21178987d100",
    // EVMos
    "9001" to "f926ff41-260d-4028-635e-91913fc28e00",
    // ZkSync
    "324" to "b310f07f-4ef7-49f3-7073-2a0a39685800",
    // Filecoin
    "314" to "5a73b3dd-af74-424e-cae0-0de859ee9400",
    // Iotx
    "4689" to "34e68754-e536-40da-c153-6ef2e7188a00",
    // Metis,
    "1088" to "3897a66d-40b9-4833-162f-a2c90531c900",
    // Moonbeam
    "1284" to "161038da-44ae-4ec7-1208-0ea569454b00",
    // Moonriver
    "1285" to "f1d73bb6-5450-4e18-38f7-fb6484264a00",
    // Zora
    "7777777" to "845c60df-d429-4991-e687-91ae45791600",
    // Celo
    "42220" to "ab781bbc-ccc6-418d-d32d-789b15da1f00",
    // Celo Alfajores
    "44787" to "ab781bbc-ccc6-418d-d32d-789b15da1f00",
    // Base
    "8453" to "7289c336-3981-4081-c5f4-efc26ac64a00",
    // Aurora
    "1313161554" to "3ff73439-a619-4894-9262-4470c773a100"
)

internal val networkNames = mapOf(
    "1" to "Ethereum",
    "42" to "Ethereum Kovan",
    "42161" to "Arbitrum",
    "43114" to "Avalanche",
    "56" to "Binance Smart Chain",
    "250" to "Fantom",
    "10" to "Optimism",
    "69" to "Optimism Kovan",
    "137" to "Polygon",
    "80001" to "Polygon Mumbai",
    "100" to "Gnosis",
    "9001" to "EVMos",
    "324" to "ZkSync",
    "314" to "Filecoin",
    "4689" to "Iotx",
    "1088" to "Metis",
    "1284" to "Moonbeam",
    "1285" to "Moonriver",
    "7777777" to "Zora",
    "42220" to "Celo",
    "44787" to "Celo Alfajores",
    "8453" to "Base",
    "1313161554" to "Aurora"
)
