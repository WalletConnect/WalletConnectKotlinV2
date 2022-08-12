package com.walletconnect.android_core.common.model

import com.walletconnect.foundation.common.model.Key

@JvmInline
internal value class SymmetricKey(override val keyAsHex: String) : Key