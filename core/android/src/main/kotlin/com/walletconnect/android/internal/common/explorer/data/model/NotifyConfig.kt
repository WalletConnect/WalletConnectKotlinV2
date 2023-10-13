@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.model

data class NotifyConfig(
    val schemaVersion: Int,
    val types: List<NotificationType>,
    val name: String,
    val description: String,
    val icons: List<String>,
)