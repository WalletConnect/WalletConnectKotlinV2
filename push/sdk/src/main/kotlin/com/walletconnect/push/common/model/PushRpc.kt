@file:JvmSynthetic

package com.walletconnect.push.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.JsonRpcClientSync
import com.walletconnect.push.dapp.json_rpc.JsonRpcMethod

internal sealed class PushRpc: JsonRpcClientSync<PushParams> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: PushParams

    @JsonClass(generateAdapter = true)
    internal data class PushRequest(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_REQUEST,
        @Json(name = "params")
        override val params: PushParams.RequestParams,
    ): PushRpc()

    @JsonClass(generateAdapter = true)
    internal data class PushMessage(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_PUSH_MESSAGE,
        @Json(name = "params")
        override val params: PushParams.MessageParams,
    ): PushRpc()

}
