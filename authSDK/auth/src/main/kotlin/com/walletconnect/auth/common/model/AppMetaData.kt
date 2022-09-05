@file:JvmSynthetic

package com.walletconnect.auth.common.model

internal data class AppMetaData(
    val name: String,
    val description: String,
    val url: String,
    val icons: List<String>,
    val redirect: String?,
)