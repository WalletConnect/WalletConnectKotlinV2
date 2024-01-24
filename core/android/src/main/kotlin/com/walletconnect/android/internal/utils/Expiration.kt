@file:JvmName("Expiration")

package com.walletconnect.android.internal.utils

val PROPOSAL_EXPIRY: Long get() = currentTimeInSeconds + fiveMinutesInSeconds
val ACTIVE_SESSION: Long get() = currentTimeInSeconds + weekInSeconds