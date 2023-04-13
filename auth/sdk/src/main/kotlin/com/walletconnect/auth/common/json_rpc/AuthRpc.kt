@file:JvmSynthetic

package com.walletconnect.auth.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.util.generateId

internal sealed class AuthRpc : JsonRpcClientSync<AuthParams> {

    @JsonClass(generateAdapter = true)
    internal data class AuthRequest(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_AUTH_REQUEST,
        @Json(name = "params")
        override val params: AuthParams.RequestParams
    ) : AuthRpc()
}