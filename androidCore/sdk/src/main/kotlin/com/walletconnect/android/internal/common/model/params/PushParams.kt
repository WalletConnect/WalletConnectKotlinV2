package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ClientParams

<<<<<<<< HEAD:androidCore/sdk/src/main/kotlin/com/walletconnect/android/internal/common/model/params/PushParams.kt
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
        @Json(name = "publicKey")
        val publicKey: String
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class MessageParams(
        @Json(name = "title")
        val title: String,
        @Json(name = "body")
        val body: String,
        @Json(name = "icon")
        val icon: String,
        @Json(name = "url")
        val url: String,
    ): PushParams()

    @JsonClass(generateAdapter = true)
    data class DeleteParams(
        @Json(name = "code")
        val code: Long,
        @Json(name = "message")
        val message: String
    ): PushParams()
}
========
//internal sealed class PushParams: ClientParams {
//
//    @JsonClass(generateAdapter = true)
//    internal data class RequestParams(
//        @Json(name = "publicKey")
//        val publicKey: String,
//        @Json(name = "metadata")
//        val metaData: AppMetaData,
//        @Json(name = "account")
//        val account: String
//    ): PushParams()
//
//    @JsonClass(generateAdapter = true)
//    internal data class RequestResponseParams(
//        @Json(name = "publicKey")
//        val publicKey: String
//    ): PushParams()
//
//    @JsonClass(generateAdapter = true)
//    internal data class MessageParams(
//        @Json(name = "title")
//        val title: String,
//        @Json(name = "body")
//        val body: String,
//        @Json(name = "icon")
//        val icon: String,
//        @Json(name = "url")
//        val url: String,
//    ): PushParams()
//}
>>>>>>>> dapp and wallet are able to communicate:push/sdk/src/main/kotlin/com/walletconnect/push/common/model/PushParams.kt
