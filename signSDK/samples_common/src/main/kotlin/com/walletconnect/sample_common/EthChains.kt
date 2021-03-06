package com.walletconnect.sample_common

import androidx.annotation.DrawableRes

private const val ETH_CHAIN = "eip155"

private val defaultEthEvents: List<String> = listOf("chainChanged", "accountChanged")

private val defaultEthMethods: List<String> = listOf(
    "eth_sendTransaction",
    "personal_sign",
    "eth_sign",
    "eth_signTypedData"
)

fun getPersonalSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray()
        .joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    return "[\"$msg\", \"$account\"]"
}

fun getEthSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray()
        .joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    return "[\"$account\", \"$msg\"]"
}

fun getEthSendTransaction(account: String): String {
    return "[{\"from\":\"$account\",\"to\":\"0x70012948c348CBF00806A3C79E3c5DAdFaAa347B\",\"data\":\"0x\",\"gasLimit\":\"0x5208\",\"gasPrice\":\"0x0649534e00\",\"value\":\"0x01\",\"nonce\":\"0x07\"}]"
}

fun getEthSignTypedData(account: String): String {
    return "[\"$account\",[\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\",{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\": \"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":\"1\",\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\": {\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}]]"
}

enum class EthChains(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: Int,
    @DrawableRes val icon: Int,
    val methods: List<String>,
    val events: List<String>,
    val order: Int,
) {

    ETHEREUM_MAIN(
        chainName = "Ethereum",
        chainNamespace = ETH_CHAIN,
        chainReference = 1,
        icon = R.drawable.ic_ethereum,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 1
    ),

    POLYGON_MATIC(
        chainName = "Polygon Matic",
        chainNamespace = ETH_CHAIN,
        chainReference = 137,
        icon = R.drawable.ic_polygon,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 2
    ),

    ETHEREUM_KOVAN(
        chainName = "Ethereum Kovan",
        chainNamespace = ETH_CHAIN,
        chainReference = 42,
        icon = R.drawable.ic_ethereum,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 3
    ),

    OPTIMISM_KOVAN(
        chainName = "Optimism Kovan",
        chainNamespace = ETH_CHAIN,
        chainReference = 69,
        icon = R.drawable.ic_optimism,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 4
    ),

    POLYGON_MUMBAI(
        chainName = "Polygon Mumbai",
        chainNamespace = ETH_CHAIN,
        chainReference = 80001,
        icon = R.drawable.ic_polygon,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 5
    ),

    ARBITRUM_RINKBY(
        chainName = "Arbitrum Rinkeby",
        chainNamespace = ETH_CHAIN,
        chainReference = 421611,
        icon = R.drawable.ic_arbitrum,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 6
    ),

    CELO_ALFAJORES(
        chainName = "Celo Alfajores",
        chainNamespace = ETH_CHAIN,
        chainReference = 44787,
        icon = R.drawable.ic_celo,
        methods = defaultEthMethods,
        events = defaultEthEvents,
        order = 7
    )
}