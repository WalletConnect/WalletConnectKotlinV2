package com.walletconnect.android.pairing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.JsonRpcClientSync

internal sealed class PairingRpc : JsonRpcClientSync<PairingParams> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: PairingParams

    @JsonClass(generateAdapter = true)
    internal data class PairingDelete(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = PairingJsonRpcMethod.WC_PAIRING_DELETE,
        @Json(name = "params")
        override val params: PairingParams.DeleteParams,
    ) : PairingRpc()

    @JsonClass(generateAdapter = true)
    internal data class PairingPing(
        @Json(name = "id")
        override val id: Long,
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = PairingJsonRpcMethod.WC_PAIRING_PING,
        @Json(name = "params")
        override val params: PairingParams.PingParams,
    ) : PairingRpc()

}