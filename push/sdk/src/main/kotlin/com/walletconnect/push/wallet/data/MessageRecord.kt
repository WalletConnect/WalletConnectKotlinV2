package com.walletconnect.push.wallet.data

data class MessageRecord(
    val id: Long,
    val topic: String,
    val publishedAt: Long,
    val message: Message
)