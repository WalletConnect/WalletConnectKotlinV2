package com.walletconnect.sample.wallet.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.convertTimestampToDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}