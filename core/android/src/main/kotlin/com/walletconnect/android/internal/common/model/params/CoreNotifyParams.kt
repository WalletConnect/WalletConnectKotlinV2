@file:JvmSynthetic

package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

sealed class CoreNotifyParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class SubscribeParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String,
    ) : CoreNotifyParams()

    @JsonClass(generateAdapter = true)
    data class SubscribeResponseParams(
        @Json(name = "responseAuth")
        val responseAuth: String,
    ) : CoreNotifyParams()

    @JsonClass(generateAdapter = true)
    data class MessageParams(
        @Json(name = "messageAuth")
        val messageAuth: String
    ) : CoreNotifyParams()

    @JsonClass(generateAdapter = true)
    data class MessageReceiptParams(
        @Json(name = "receiptAuth")
        val receiptAuth: String
    ) : CoreNotifyParams()

    @JsonClass(generateAdapter = true)
    data class UpdateParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String,
    ) : CoreNotifyParams()

    @JsonClass(generateAdapter = true)
    data class DeleteParams(
        @Json(name = "deleteAuth")
        val deleteAuth: String,
    ) : CoreNotifyParams()
}