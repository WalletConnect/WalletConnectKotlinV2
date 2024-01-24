@file:JvmName("Time")

package com.walletconnect.android.internal.utils

import java.util.concurrent.TimeUnit

val currentTimeInSeconds: Long get() = System.currentTimeMillis() / 1000
val thirtySeconds: Long = TimeUnit.SECONDS.convert(30, TimeUnit.SECONDS)
val fiveMinutesInSeconds: Long = TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES)
val dayInSeconds: Long = TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
val weekInSeconds: Long = TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
val monthInSeconds: Long = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)