@file:JvmSynthetic

package com.walletconnect.auth.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.impl.common.model.type.JsonRpcClientSync
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod

internal sealed class AuthRpc : JsonRpcClientSync<AuthParams> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: AuthParams

    @JsonClass(generateAdapter = true)
    internal data class AuthRequest(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_AUTH_REQUEST,
        @Json(name = "params")
        override val params: AuthParams.RequestParams
    ) : AuthRpc()
}