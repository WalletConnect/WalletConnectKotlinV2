@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.notify.common.JsonRpcMethod
import com.walletconnect.util.generateId

internal sealed class NotifyRpc : JsonRpcClientSync<CoreNotifyParams> {

    @JsonClass(generateAdapter = true)
    internal data class NotifySubscribe(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_SUBSCRIBE,
        @Json(name = "params")
        override val params: CoreNotifyParams.SubscribeParams,
    ): NotifyRpc()

    @JsonClass(generateAdapter = true)
    internal data class NotifyDelete(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_DELETE,
        @Json(name = "params")
        override val params: CoreNotifyParams.DeleteParams,
    ) : NotifyRpc()

    @JsonClass(generateAdapter = true)
    internal data class NotifyUpdate(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_UPDATE,
        @Json(name = "params")
        override val params: CoreNotifyParams.UpdateParams,
    ): NotifyRpc()

    @JsonClass(generateAdapter = true)
    internal data class NotifyMessage(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_MESSAGE,
        @Json(name = "params")
        override val params: CoreNotifyParams.MessageParams,
    ) : NotifyRpc()

    @JsonClass(generateAdapter = true)
    internal data class NotifyWatchSubscriptions(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_WATCH_SUBSCRIPTIONS,
        @Json(name = "params")
        override val params: CoreNotifyParams.WatchSubscriptionsParams,
    ): NotifyRpc()

    @JsonClass(generateAdapter = true)
    internal data class NotifySubscriptionsChanged(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_NOTIFY_SUBSCRIPTIONS_CHANGED,
        @Json(name = "params")
        override val params: CoreNotifyParams.SubscriptionsChangedParams,
    ) : NotifyRpc()
}
