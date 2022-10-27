@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class Thread(
    val topic: String,
    val selfAccount: String,
    val peerAccount: String,
)