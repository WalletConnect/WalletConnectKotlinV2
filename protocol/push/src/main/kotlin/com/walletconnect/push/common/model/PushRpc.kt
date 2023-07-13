@file:JvmSynthetic

package com.walletconnect.push.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.util.generateId

internal sealed class PushRpc : JsonRpcClientSync<PushParams> {

    @JsonClass(generateAdapter = true)
    internal data class PushPropose(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_PROPOSE,
        @Json(name = "params")
        override val params: PushParams.ProposeParams,
    ) : PushRpc()

    @JsonClass(generateAdapter = true)
    internal data class PushMessage(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_MESSAGE,
        @Json(name = "params")
        override val params: PushParams.MessageParams,
    ) : PushRpc()

    @JsonClass(generateAdapter = true)
    internal data class PushDelete(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_DELETE,
        @Json(name = "params")
        override val params: PushParams.DeleteParams,
    ) : PushRpc()

    @JsonClass(generateAdapter = true)
    internal data class PushSubscribe(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_SUBSCRIBE,
        @Json(name = "params")
        override val params: PushParams.SubscribeParams,
    ): PushRpc()

    @JsonClass(generateAdapter = true)
    internal data class PushUpdate(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_UPDATE,
        @Json(name = "params")
        override val params: PushParams.UpdateParams,
    ): PushRpc()
}
