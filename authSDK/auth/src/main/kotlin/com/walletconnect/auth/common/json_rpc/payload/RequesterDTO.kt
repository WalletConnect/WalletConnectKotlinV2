@file:JvmSynthetic

package com.walletconnect.auth.common.json_rpc.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android_core.common.model.MetaData

//todo: Discuss: Should VO classes in json_rpc be DTO
@JsonClass(generateAdapter = true)
internal data class RequesterDTO(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: MetaData,
)