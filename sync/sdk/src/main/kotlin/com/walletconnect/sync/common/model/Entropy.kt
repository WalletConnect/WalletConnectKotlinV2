@file:JvmSynthetic

package com.walletconnect.sync.common.model

import com.walletconnect.android.internal.common.crypto.sha256

@JvmInline
internal value class Entropy(val value: String)

@JvmSynthetic
internal fun String.toEntropy() = Entropy(sha256(this.toByteArray()))