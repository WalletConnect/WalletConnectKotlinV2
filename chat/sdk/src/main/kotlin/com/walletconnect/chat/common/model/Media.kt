@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Media(
    val type: String,
    val data: String,
)