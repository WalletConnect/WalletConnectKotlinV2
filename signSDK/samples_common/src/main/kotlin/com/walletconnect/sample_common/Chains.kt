package com.walletconnect.sample_common

import androidx.annotation.DrawableRes

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

enum class Chains(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: String,
    @DrawableRes val icon: Int,
    val methods: List<String>,
    val events: List<String>,
    val order: Int,
    val chainId: String = "$chainNamespace:$chainReference"
) {

    ETHEREUM_MAIN(
        chainName = "Ethereum",
        chainNamespace = Info.Eth.chain,
        chainReference = "1",
        icon = R.drawable.ic_ethereum,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 1
    ),

    POLYGON_MATIC(
        chainName = "Polygon Matic",
        chainNamespace = Info.Eth.chain,
        chainReference = "137",
        icon = R.drawable.ic_polygon,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 2
    ),

    ETHEREUM_KOVAN(
        chainName = "Ethereum Kovan",
        chainNamespace = Info.Eth.chain,
        chainReference = "42",
        icon = R.drawable.ic_ethereum,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 3
    ),

    OPTIMISM_KOVAN(
        chainName = "Optimism Kovan",
        chainNamespace = Info.Eth.chain,
        chainReference = "69",
        icon = R.drawable.ic_optimism,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 4
    ),

    POLYGON_MUMBAI(
        chainName = "Polygon Mumbai",
        chainNamespace = Info.Eth.chain,
        chainReference = "80001",
        icon = R.drawable.ic_polygon,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 5
    ),

    ARBITRUM_RINKBY(
        chainName = "Arbitrum Rinkeby",
        chainNamespace = Info.Eth.chain,
        chainReference = "421611",
        icon = R.drawable.ic_arbitrum,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 6
    ),

    CELO_ALFAJORES(
        chainName = "Celo Alfajores",
        chainNamespace = Info.Eth.chain,
        chainReference = "44787",
        icon = R.drawable.ic_celo,
        methods = Info.Eth.defaultMethods,
        events = Info.Eth.defaultEvents,
        order = 7
    ),
    COSMOS(
        chainName = "Cosmos",
        chainNamespace = Info.Cosmos.chain,
        chainReference = "cosmoshub-4",
        icon = R.drawable.ic_cosmos,
        methods = Info.Cosmos.defaultMethods,
        events = Info.Cosmos.defaultEvents,
        order = 7
    );

    sealed class Info {
        abstract val chain: String
        abstract val defaultEvents: List<String>
        abstract val defaultMethods: List<String>

        object Eth: Info() {
            override val chain = "eip155"
            override val defaultEvents: List<String> = listOf("chainChanged", "accountChanged")
            override val defaultMethods: List<String> = listOf(
                "eth_sendTransaction",
                "personal_sign",
                "eth_sign",
                "eth_signTypedData"
            )
        }

        object Cosmos: Info() {
            override val chain = "cosmos"
            override val defaultEvents: List<String> = listOf("chainChanged", "accountChanged")
            override val defaultMethods: List<String> = listOf(
                "cosmos_signDirect",
                "cosmos_signAmino"
            )
        }
    }
}