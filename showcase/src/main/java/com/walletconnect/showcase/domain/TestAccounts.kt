package com.walletconnect.showcase.domain

import com.walletconnect.sample_common.Chains


const val ACCOUNTS_1_EIP155_ADDRESS = "0x46586f7F766955CAF22A54dDA7570E6eFA94c16c"
const val ACCOUNTS_N_EIP155_ADDRESS = "0x0000000000000000000000000000000000000000"

fun eip155Address(n: Int): String =
    n.toString(16).let { nAsHex -> ACCOUNTS_N_EIP155_ADDRESS.takeLast(nAsHex.length) + nAsHex }


val accounts: List<Pair<Chains, String>> = listOf(
    Chains.ETHEREUM_MAIN to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.POLYGON_MATIC to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.ETHEREUM_KOVAN to ACCOUNTS_1_EIP155_ADDRESS,
//    Chains.OPTIMISM_KOVAN to "0xf5de760f2e916647fd766b4ad9e85ff943ce3a2b",
    Chains.POLYGON_MUMBAI to ACCOUNTS_1_EIP155_ADDRESS,
//    Chains.ARBITRUM_RINKBY to "0x682570add15588df8c3506eef2e737db29266de2",
//    Chains.CELO_ALFAJORES to "0xdD5Cb02066fde415dda4f04EE53fBb652066afEE",
    Chains.COSMOS to "cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc"
)

val PRIVATE_KEY_1: ByteArray = "e05c1a7f048a164ab400e38764708a401c773fa83181b923fc8b2724f46c0c6c".hexToBytes()

const val ISS_DID_PREFIX = "did:pkh:"

val ISSUER = accounts.map { it.toIssuer() }.first()

fun Pair<Chains, String>.toIssuer(): String = "$ISS_DID_PREFIX${first.chainId}:$second"

fun String.hexToBytes(): ByteArray {
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