package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.walletconnect.android.internal.common.model.AppMetaData

data class Requester(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: AppMetaData
)