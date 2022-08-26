package com.walletconnect.auth.common.json_rpc.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO
import com.walletconnect.auth.common.json_rpc.payload.RequesterDTO


internal sealed class AuthParams : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class RequestParams(
        @Json(name = "requester")
        val requester: RequesterDTO,
        @Json(name = "payloadParams")
        val payloadParams: PayloadParamsDTO,
    ) : AuthParams()

    @JsonClass(generateAdapter = true)
    internal data class ResponseParams(
        @Json(name = "cacao")
        val cacao: CacaoDTO,
    ) : AuthParams()
}