package com.walletconnect.foundation.common.model

interface Key {
    val keyAsHex: String
}

@JvmInline
value class SymmetricKey(override val keyAsHex: String) : Key

@JvmInline
value class PublicKey(override val keyAsHex: String) : Key

@JvmInline
value class PrivateKey(override val keyAsHex: String) : Key