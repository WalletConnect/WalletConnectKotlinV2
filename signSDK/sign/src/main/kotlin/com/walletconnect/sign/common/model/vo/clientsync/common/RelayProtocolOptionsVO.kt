@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RelayProtocolOptionsVO(
    val protocol: String = "irn",
    val data: String? = null
)