package com.walletconnect.auth.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.ClientParams
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
        @Json(name = "h")
        val header: Cacao.Header,
        @Json(name = "p")
        val payload: Cacao.Payload,
        @Json(name = "s")
        val signature: Cacao.Signature,
    ) : AuthParams()
}