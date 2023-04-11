package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.utils.HexPrefix

@JvmSynthetic
internal fun String.guaranteeNoHexPrefix(): String = removePrefix(String.HexPrefix)