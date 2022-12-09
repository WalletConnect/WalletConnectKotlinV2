@file:JvmSynthetic

package com.walletconnect.push.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ClientParams

internal sealed class PushParams: ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class PushRequestParams(
        @Json(name = "publicKey")
        val publicKey: String,
        @Json(name = "metadata")
        val metaData: AppMetaData,
        @Json(name = "account")
        val account: String
    ): PushParams()

    @JsonClass(generateAdapter = true)
    internal data class PushMessageParams(
        @Json(name = "title")
        val title: String,
        @Json(name = "body")
        val body: String,
        @Json(name = "icon")
        val icon: String,
        @Json(name = "url")
        val url: String,
    ): PushParams()
}
