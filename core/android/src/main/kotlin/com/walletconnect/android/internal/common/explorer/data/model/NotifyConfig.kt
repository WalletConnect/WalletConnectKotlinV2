@file:JvmSynthetic

package com.walletconnect.android.internal.common.explorer.data.model

data class NotifyConfig(
    val dappUrl: String,
    val name: String,
    val homepage: String,
    val description: String,
    val types: List<NotificationType>,
    val imageUrl: ImageUrl,
    val isVerified: Boolean,
)


data class NotificationType(
    val name: String,
    val id: String,
    val description: String,
)