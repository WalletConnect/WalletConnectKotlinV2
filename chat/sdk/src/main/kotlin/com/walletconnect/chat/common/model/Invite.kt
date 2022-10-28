@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal data class Invite(
    val account: String,
    val message: String,
    val signature: String? = null
)