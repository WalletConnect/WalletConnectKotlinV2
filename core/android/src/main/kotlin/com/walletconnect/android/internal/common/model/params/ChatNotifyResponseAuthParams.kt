package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

interface ChatNotifyResponseAuthParams {

    @JsonClass(generateAdapter = true)
    data class ResponseAuth(
        @Json(name = "responseAuth")
        val responseAuth: String,
    ) : ClientParams
}