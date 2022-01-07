package com.walletconnect.walletconnectv2.crypto.model

interface Key {
    val keyAsHex: String
}

@JvmInline
value class PublicKey(override val keyAsHex: String) : Key

@JvmInline
value class PrivateKey(override val keyAsHex: String) : Key

@JvmInline
value class SharedKey(override val keyAsHex: String) : Key