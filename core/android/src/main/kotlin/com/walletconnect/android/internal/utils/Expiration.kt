@file:JvmName("Expiration")

package com.walletconnect.android.internal.utils

val PROPOSAL_EXPIRY: Long get() = CURRENT_TIME_IN_SECONDS + THIRTY_SECONDS//FIVE_MINUTES_IN_SECONDS
val ACTIVE_SESSION: Long get() = CURRENT_TIME_IN_SECONDS + WEEK_IN_SECONDS