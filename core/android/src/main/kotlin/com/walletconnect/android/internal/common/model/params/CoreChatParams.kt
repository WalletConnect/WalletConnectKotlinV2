package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

sealed interface CoreChatParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class ReceiptParams(
        @Json(name = "receiptAuth")
        val receiptAuth: String,
    ) : CoreChatParams
}