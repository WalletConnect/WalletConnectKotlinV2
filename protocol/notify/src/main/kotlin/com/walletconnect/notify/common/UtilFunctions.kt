@file:JvmSynthetic

package com.walletconnect.notify.common

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.utils.monthInSeconds
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun calcExpiry(): Expiry {
    val currentTimeMs = System.currentTimeMillis()
    val currentTimeSeconds = TimeUnit.SECONDS.convert(currentTimeMs, TimeUnit.MILLISECONDS)
    val expiryTimeSeconds = currentTimeSeconds + monthInSeconds

    return Expiry(expiryTimeSeconds)
}


@JvmSynthetic
internal fun convertToUTF8(input: String): String {
    val bytes = input.toByteArray(Charset.forName("ISO-8859-1"))
    return String(bytes, Charset.forName("UTF-8"))
}
