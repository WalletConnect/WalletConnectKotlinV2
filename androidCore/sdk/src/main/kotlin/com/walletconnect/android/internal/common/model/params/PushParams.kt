package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.type.ClientParams

sealed class PushParams: ClientParams {

    @JsonClass(generateAdapter = true)
    data class RequestParams(
        @Json(name = "publicKey")
        val publicKey: String,
        @Json(name = "metadata")
        val metaData: AppMetaData,
        @Json(name = "account")
        val account: String
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class RequestResponseParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String
    ): PushParams()

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
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class DeleteParams(
        @Json(name = "code")
        val code: Long,
        @Json(name = "message")
        val message: String
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class SubscribeParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class UpdateParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String
    ): PushParams()
}