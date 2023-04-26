@file:JvmSynthetic

package com.walletconnect.android.sync.common.model

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.util.hexToBytes

@JvmInline
internal value class Entropy(val value: String) {
    fun toBytes(): ByteArray = value.hexToBytes()
}

@JvmSynthetic
internal fun String.toEntropy() = Entropy(sha256(this.toByteArray()))