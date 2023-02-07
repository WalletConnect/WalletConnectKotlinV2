package com.walletconnect.foundation.common.model

import com.walletconnect.util.hexToBytes

interface Key {
    val keyAsHex: String
    val keyAsBytes: ByteArray
        get() = keyAsHex.hexToBytes()
}

@JvmInline
value class PublicKey(override val keyAsHex: String) : Key

@JvmInline
value class PrivateKey(override val keyAsHex: String) : Key