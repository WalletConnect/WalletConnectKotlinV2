@file:JvmSynthetic

package com.walletconnect.notify.common.model

internal data class NotifyMessage(
    val title: String,
    val body: String,
    val icon: String?,
    val url: String?,
    val type: String,
)

