@file:JvmName("Utils")
@file:Suppress("PackageDirectoryMismatch")

package com.walletconnect.utils

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.sign.util.Time

@JvmSynthetic
internal fun Expiry.isSequenceValid(): Boolean = seconds > Time.currentTimeInSeconds