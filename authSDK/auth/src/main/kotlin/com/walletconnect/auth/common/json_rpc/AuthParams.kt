package com.walletconnect.auth.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.auth.common.model.Cacao
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.common.model.Requester


internal sealed class AuthParams : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class RequestParams(
        @Json(name = "requester")
        val requester: Requester,
        @Json(name = "payloadParams")
        val payloadParams: PayloadParams,
    ) : AuthParams()

    @JsonClass(generateAdapter = true)
    internal data class ResponseParams(
        @Json(name = "header")
        val header: Cacao.Header,
        @Json(name = "payload")
        val payload: Cacao.Payload,
        @Json(name = "signature")
        val signature: Cacao.Signature,
    ) : AuthParams()
}