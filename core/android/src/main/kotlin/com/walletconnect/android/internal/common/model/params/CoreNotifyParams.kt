@file:JvmSynthetic

package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

sealed interface CoreNotifyParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class SubscribeParams(
        @Json(name = "subscriptionAuth")
        val subscriptionAuth: String,
    ) : CoreNotifyParams

    @JsonClass(generateAdapter = true)
    data class MessageParams(
        @Json(name = "messageAuth")
        val messageAuth: String,
    ) : CoreNotifyParams

    @JsonClass(generateAdapter = true)
    data class UpdateParams(
        @Json(name = "updateAuth")
        val updateAuth: String,
    ) : CoreNotifyParams

    @JsonClass(generateAdapter = true)
    data class DeleteParams(
        @Json(name = "deleteAuth")
        val deleteAuth: String,
    ) : CoreNotifyParams

    @JsonClass(generateAdapter = true)
    data class WatchSubscriptionsParams(
        @Json(name = "watchSubscriptionsAuth")
        val watchSubscriptionsAuth: String,
    ) : CoreNotifyParams

    @JsonClass(generateAdapter = true)
    data class SubscriptionsChangedParams(
        @Json(name = "subscriptionsChangedAuth")
        val subscriptionsChangedAuth: String,
    ) : CoreNotifyParams
}