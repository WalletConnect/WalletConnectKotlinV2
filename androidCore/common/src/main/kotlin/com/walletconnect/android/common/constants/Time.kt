@file:JvmName("Time")

package com.walletconnect.android.common.constants

import java.util.concurrent.TimeUnit

val CURRENT_TIME_IN_SECONDS: Long = System.currentTimeMillis() / 1000
val THIRTY_SECONDS: Long = TimeUnit.SECONDS.convert(30, TimeUnit.SECONDS)
val FIVE_MINUTES_IN_SECONDS: Long = TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES)
val DAY_IN_SECONDS: Long = TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
val WEEK_IN_SECONDS: Long = TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
val MONTH_IN_SECONDS: Long = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)