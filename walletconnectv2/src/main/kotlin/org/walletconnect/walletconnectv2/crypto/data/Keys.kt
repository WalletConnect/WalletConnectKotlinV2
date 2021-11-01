package org.walletconnect.walletconnectv2.crypto.data

interface Key {
    val keyAsHex: String
}

@JvmInline
value class PublicKey(override val keyAsHex: String): Key

@JvmInline
value class PrivateKey(override val keyAsHex: String): Key