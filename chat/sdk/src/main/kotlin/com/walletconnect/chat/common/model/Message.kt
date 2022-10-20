@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class Message(
    val message: String,
    val authorAccount: String,
    val timestamp: Long,
    val media: Media
)