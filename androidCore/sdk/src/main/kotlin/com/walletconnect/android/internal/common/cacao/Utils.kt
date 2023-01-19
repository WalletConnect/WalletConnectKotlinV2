package com.walletconnect.android.internal.common.cacao

import com.walletconnect.utils.HexPrefix

@JvmSynthetic
internal fun String.guaranteeNoHexPrefix(): String = removePrefix(String.HexPrefix)