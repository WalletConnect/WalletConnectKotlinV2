package com.walletconnect.dapp.domain

import androidx.annotation.DrawableRes
import com.walletconnect.dapp.R

private val defaultEthMethods: List<String> = listOf("eth_sendTransaction", "personal_sign", "eth_signTypedData")

fun getPersonalSignBody(account: String): String {
    val msg = "My email is john@doe.com - ${System.currentTimeMillis()}".encodeToByteArray().joinToString(separator = "", prefix = "0x") { eachByte -> "%02x".format(eachByte) }
    return """[$msg, $account]"""
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

private fun String.hexToBytes(): ByteArray {
    val len = this.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(this[i], 16) shl 4)
                + Character.digit(this[i + 1], 16)).toByte()
        i += 2
    }
    return data
}

enum class Chains(val chainName: String, val parentChain: String, val chainId: Int, @DrawableRes val icon: Int, val methods: List<String>) {

    ETHEREUM_KOVAN(
        chainName = "Ethereum Kovan",
        parentChain = "eip155",
        chainId = 42,
        icon = R.drawable.ic_ethereum,
        methods = defaultEthMethods
    ),

    OPTIMISM_KOVAN(
        chainName = "Optimism Kovan",
        parentChain = "eip155",
        chainId = 69,
        icon = R.drawable.ic_optimism,
        methods = defaultEthMethods
    ),

    POLYGON_MUMBAI(
        chainName = "Polygon Mumbai",
        parentChain = "eip155",
        chainId = 80001,
        icon = R.drawable.ic_polygon,
        methods = defaultEthMethods
    ),

    ARBITRUM_RINKBY(
        chainName = "Arbitrum Rinkeby",
        parentChain = "eip155",
        chainId = 421611,
        icon = R.drawable.ic_arbitrum,
        methods = defaultEthMethods
    ),

    CELO_ALFAJORES(
        chainName = "Celo Alfajores",
        parentChain = "eip155",
        chainId = 44787,
        icon = R.drawable.ic_celo,
        methods = defaultEthMethods
    )
}