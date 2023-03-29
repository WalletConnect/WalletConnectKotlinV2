package com.walletconnect.sample.wallet.domain

import com.walletconnect.sample_common.Chains


const val ACCOUNTS_1_EIP155_ADDRESS = "0x46586f7F766955CAF22A54dDA7570E6eFA94c16c"
const val ACCOUNTS_N_EIP155_ADDRESS = "0x0000000000000000000000000000000000000000"

fun eip155Address(n: Int): String =
    n.toString(16).let { nAsHex -> ACCOUNTS_N_EIP155_ADDRESS.takeLast(nAsHex.length) + nAsHex }


val accounts: List<Pair<Chains, String>> = listOf(
    Chains.ETHEREUM_MAIN to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.POLYGON_MATIC to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.ETHEREUM_KOVAN to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.POLYGON_MUMBAI to ACCOUNTS_1_EIP155_ADDRESS,
    Chains.COSMOS to "cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc",
    Chains.BNB to ACCOUNTS_1_EIP155_ADDRESS
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