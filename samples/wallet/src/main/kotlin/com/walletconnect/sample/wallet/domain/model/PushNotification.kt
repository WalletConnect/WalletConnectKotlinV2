package com.walletconnect.sample.wallet.domain.model

data class PushNotification(
    val title: String,
    val message: String,
    val url: String,
    val date: String,
)