package com.walletconnect.dapp.domain

import androidx.annotation.DrawableRes
import com.walletconnect.dapp.R

private const val ETH_CHAIN = "eip155"
// Commented out other methods due to wallet not understanding request
// TODO: Fix other RPC methods
private val defaultEthMethods: List<String> = listOf(
//    "eth_sendTransaction",
    "personal_sign"
//    "eth_signTypedData"
)


fun getPersonalSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray().joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    return "[\"$msg\", \"$account\"]"
}

fun getEthSendTransaction(account: String): String {
    return """
        [
            {
                from: "$account",
                to: "0xd46e8dd67c5d32be8058bb8eb970870f07244567",
                data: "0xd46e8dd67c5d32be8d46e8dd67c5d32be8058bb8eb970870f072445675058bb8eb970870f072445675",
                gas: "0x76c0",
                gasPrice: "0x9184e72a000",
                value: "0x9184e72a",
                nonce: "0x117"
            },
        ]
    """.trimIndent()
}

fun getEthSignTypedData(account: String): String {
    return """
        [
            "$account",
            [
                "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826",
                {
                    types: {
                        EIP712Domain: [
                            {
                                name: "name",
                                type: "string",
                            },
                            {
                                name: "version",
                                type: "string",
                            },
                            {
                                name: "chainId",
                                type: "uint256",
                            },
                            {
                                name: "verifyingContract",
                                type: "address",
                            },
                        ],
                        Person: [
                            {
                                name: "name",
                                type: "string",
                            },
                            {
                                name: "wallet",
                                type: "address",
                            },
                        ],
                        Mail: [
                            {
                                name: "from",
                                type: "Person",
                            },
                            {
                                name: "to",
                                type: "Person",
                            },
                            {
                                name: "contents",
                                type: "string",
                            },
                        ],
                    },
                    primaryType: "Mail",
                    domain: {
                        name: "Ether Mail",
                        version: "1",
                        chainId: 1,
                        verifyingContract: "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC",
                    },
                    message: {
                        from: {
                            name: "Cow",
                            wallet: "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826",
                        },
                        to: {
                            name: "Bob",
                            wallet: "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB",
                        },
                        contents: "Hello, Bob!",
                    },
                }
            ]
        ]
    """.trimIndent()
}

enum class Chains(val chainName: String, val parentChain: String, val chainId: Int, @DrawableRes val icon: Int, val methods: List<String>) {

    ETHEREUM_KOVAN(
        chainName = "Ethereum Kovan",
        parentChain = ETH_CHAIN,
        chainId = 42,
        icon = R.drawable.ic_ethereum,
        methods = defaultEthMethods
    ),

    OPTIMISM_KOVAN(
        chainName = "Optimism Kovan",
        parentChain = ETH_CHAIN,
        chainId = 69,
        icon = R.drawable.ic_optimism,
        methods = defaultEthMethods
    ),

    POLYGON_MUMBAI(
        chainName = "Polygon Mumbai",
        parentChain = ETH_CHAIN,
        chainId = 80001,
        icon = R.drawable.ic_polygon,
        methods = defaultEthMethods
    ),

    ARBITRUM_RINKBY(
        chainName = "Arbitrum Rinkeby",
        parentChain = ETH_CHAIN,
        chainId = 421611,
        icon = R.drawable.ic_arbitrum,
        methods = defaultEthMethods
    ),

    CELO_ALFAJORES(
        chainName = "Celo Alfajores",
        parentChain = ETH_CHAIN,
        chainId = 44787,
        icon = R.drawable.ic_celo,
        methods = defaultEthMethods
    )
}