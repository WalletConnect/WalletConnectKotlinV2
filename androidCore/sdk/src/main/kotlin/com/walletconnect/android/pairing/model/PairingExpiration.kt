@file:JvmName("Expiration")

package com.walletconnect.android.pairing.model

import com.walletconnect.android.internal.common.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.internal.common.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.internal.common.MONTH_IN_SECONDS

val INACTIVE_PAIRING: Long = CURRENT_TIME_IN_SECONDS + FIVE_MINUTES_IN_SECONDS
val ACTIVE_PAIRING: Long = CURRENT_TIME_IN_SECONDS + MONTH_IN_SECONDS