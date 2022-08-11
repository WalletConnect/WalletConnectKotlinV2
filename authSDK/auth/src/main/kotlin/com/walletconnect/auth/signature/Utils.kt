package com.walletconnect.auth.signature

import com.walletconnect.utils.HexPrefix

@JvmSynthetic
internal fun String.guaranteeNoHexPrefix(): String = removePrefix(String.HexPrefix)
