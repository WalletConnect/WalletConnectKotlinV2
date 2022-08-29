@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.android_core.utils.CURRENT_TIME_IN_SECONDS

@get:JvmSynthetic
val String.Companion.Empty
    get() = ""

@get:JvmSynthetic
val Int.Companion.DefaultId
    get() = -1

@JvmSynthetic
fun Long.extractTimestamp() = this / 1000

@JvmSynthetic
fun Expiry.isSequenceValid(): Boolean = seconds > CURRENT_TIME_IN_SECONDS

@get:JvmSynthetic
val String.Companion.HexPrefix
    get() = "0x"
