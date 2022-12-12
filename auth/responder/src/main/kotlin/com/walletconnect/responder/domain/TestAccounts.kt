package com.walletconnect.responder.domain

import com.walletconnect.sample_common.Chains


const val ACCOUNTS_1_ADDRESS = "0x46586f7F766955CAF22A54dDA7570E6eFA94c16c"

val mapOfAccounts1: Map<Chains, String> = mapOf(
    Chains.ETHEREUM_MAIN to ACCOUNTS_1_ADDRESS,
)

val PRIVATE_KEY_1: ByteArray = "e05c1a7f048a164ab400e38764708a401c773fa83181b923fc8b2724f46c0c6c".hexToBytes()

const val ISS_DID_PREFIX = "did:pkh:"

val ISSUER = mapOfAccounts1.map { it.toIssuer() }.first()

fun Map.Entry<Chains, String>.toIssuer(): String = "$ISS_DID_PREFIX${key.chainId}:$value"

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