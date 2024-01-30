@file:JvmSynthetic

package com.walletconnect.notify.common.model

internal data class NotificationMessage(
    val title: String,
    val body: String,
    val icon: String?,
    val url: String?,
    val type: String,
)