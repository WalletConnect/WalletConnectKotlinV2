package org.walletconnect.walletconnectv2.crypto.data

interface Key {
    val key: String
}

@JvmInline
value class PublicKey(override val key: String): Key

@JvmInline
value class PrivateKey(override val key: String): Key