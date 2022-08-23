package com.walletconnect.responder.domain

import com.walletconnect.sample_common.Chains


const val ACCOUNTS_1_ID = 1
const val ACCOUNTS_1_ADDRESS = "0x46586f7f766955caf22a54dda7570e6efa94c16c"

val mapOfAccounts1: Map<Chains, String> = mapOf(
    Chains.ETHEREUM_MAIN to ACCOUNTS_1_ADDRESS,
)

const val ACCOUNTS_2_ID = 2
const val ACCOUNTS_2_ADDRESS = "0x3a16bd62a7eaa3428c7483f764e9ad1da526755e"
val mapOfAccounts2: Map<Chains, String> = mapOf(
    Chains.ETHEREUM_MAIN to ACCOUNTS_2_ADDRESS,
)

val mapOfAllAccounts = mapOf(
    ACCOUNTS_1_ID to mapOfAccounts1,
//    ACCOUNTS_2_ID to mapOfAccounts2 // todo: Add support to signing with different account after initalization
)

val PRIVATE_KEY_1: ByteArray = "e05c1a7f048a164ab400e38764708a401c773fa83181b923fc8b2724f46c0c6c".hexToBytes()
val PRIVATE_KEY_2: ByteArray = "c06f6f6fce064eea7e645597f90d633a4837d879c02c9c402ac6e5b1be3ed243".hexToBytes()

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