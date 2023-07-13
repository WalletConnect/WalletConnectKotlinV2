package com.walletconnect.sample.wallet.domain

import com.walletconnect.sample_common.Chains
import com.walletconnect.util.hexToBytes
import io.ipfs.multibase.Base16

val ACCOUNTS_1_EIP155_ADDRESS by lazy { EthAccountDelegate.account }

val accounts: List<Pair<Chains, String>> by lazy {
    listOf(
        Chains.ETHEREUM_MAIN to ACCOUNTS_1_EIP155_ADDRESS,
        Chains.POLYGON_MATIC to ACCOUNTS_1_EIP155_ADDRESS,
        Chains.ETHEREUM_KOVAN to ACCOUNTS_1_EIP155_ADDRESS,
        Chains.POLYGON_MUMBAI to ACCOUNTS_1_EIP155_ADDRESS,
        Chains.COSMOS to "cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc",
        Chains.BNB to ACCOUNTS_1_EIP155_ADDRESS
    )
}

val PRIVATE_KEY_1: ByteArray by lazy { EthAccountDelegate.privateKey.hexToBytes() }

const val ISS_DID_PREFIX = "did:pkh:"

val ISSUER by lazy { accounts.map { it.toIssuer() }.first() }

fun Pair<Chains, String>.toIssuer(): String = "$ISS_DID_PREFIX${first.chainId}:$second"

fun ByteArray.bytesToHex(): String = Base16.encode(this)

fun String.hexToBytes(): ByteArray = Base16.decode(this.lowercase())