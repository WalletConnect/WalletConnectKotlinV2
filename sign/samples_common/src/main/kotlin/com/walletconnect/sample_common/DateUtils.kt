package com.walletconnect.sample_common

import java.text.SimpleDateFormat
import java.util.*

fun Long.convertSecondsToDate(): String {
    val date = Date(this * 1000)
    val format = SimpleDateFormat("yyyy/MM/dd - HH:mm")
    return format.format(date)
}