package com.walletconnect.auth.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams
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
}