@file:JvmSynthetic

package com.walletconnect.notify.common

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun calcExpiry(): Expiry {
    val currentTimeMs = System.currentTimeMillis()
    val currentTimeSeconds = TimeUnit.SECONDS.convert(currentTimeMs, TimeUnit.MILLISECONDS)
    val expiryTimeSeconds = currentTimeSeconds + MONTH_IN_SECONDS

    return Expiry(expiryTimeSeconds)
}