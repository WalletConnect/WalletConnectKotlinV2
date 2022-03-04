package com.walletconnect.walletconnectv2.util

import java.util.concurrent.TimeUnit

object Time {
    val hourInMillis: Long = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
    val dayInMillis: Long = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
    val weekInMillis: Long = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS)
    val monthInMillis: Long = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)
}