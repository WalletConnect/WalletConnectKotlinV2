@file:JvmSynthetic

package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.model.type.ClientParams

sealed class NotifyParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class SubscribeParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String,
    ) : NotifyParams()

    @JsonClass(generateAdapter = true)
    data class MessageParams(
        @Json(name = "title")
        val title: String,
        @Json(name = "body")
        val body: String,
        @Json(name = "icon")
        val icon: String?,
        @Json(name = "url")
        val url: String?,
        @Json(name = "type")
        val type: String,
    ) : NotifyParams()

    @JsonClass(generateAdapter = true)
    data class UpdateParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String,
    ) : NotifyParams()

    @JsonClass(generateAdapter = true)
    data class DeleteParams(
        @Json(name = "code")
        val code: Long,
        @Json(name = "message")
        val message: String,
    ) : NotifyParams() {

        constructor(error: Reason.UserDisconnected = Reason.UserDisconnected) : this(error.code.toLong(), error.message)
    }
}