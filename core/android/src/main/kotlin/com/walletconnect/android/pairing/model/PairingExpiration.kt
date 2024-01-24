@file:JvmName("Expiration")

package com.walletconnect.android.pairing.model

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.internal.utils.monthInSeconds

val INACTIVE_PAIRING: Long get() = currentTimeInSeconds + fiveMinutesInSeconds
val ACTIVE_PAIRING: Long get() = currentTimeInSeconds + monthInSeconds