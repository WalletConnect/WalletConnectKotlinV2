package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

open class CoreChatParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class AcceptanceParams(
        @Json(name = "responseAuth")
        val responseAuth: String,
    ) : CoreChatParams()
}