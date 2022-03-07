package com.walletconnect.walletconnectv2.util

import java.util.concurrent.TimeUnit

object Time {
    val hourInSeconds: Long = TimeUnit.SECONDS.convert(1, TimeUnit.HOURS)
    val dayInSeconds: Long = TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
    val weekInSeconds: Long = TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
    val monthInSeconds: Long = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS)
}