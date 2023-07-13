package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.signing.cacao.Cacao

open class CoreAuthParams : ClientParams {
    @JsonClass(generateAdapter = true)
    data class ResponseParams(
        @Json(name = "h")
        val header: Cacao.Header,
        @Json(name = "p")
        val payload: Cacao.Payload,
        @Json(name = "s")
        val signature: Cacao.Signature,
    ) : CoreAuthParams()
}