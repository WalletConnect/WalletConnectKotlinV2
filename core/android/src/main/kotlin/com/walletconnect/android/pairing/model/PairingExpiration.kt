@file:JvmName("Expiration")

package com.walletconnect.android.pairing.model

import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import java.util.concurrent.TimeUnit

val INACTIVE_PAIRING: Long get() = CURRENT_TIME_IN_SECONDS + THIRTY_SECONDS//FIVE_MINUTES_IN_SECONDS
val ACTIVE_PAIRING: Long get() = CURRENT_TIME_IN_SECONDS + TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)//MONTH_IN_SECONDS