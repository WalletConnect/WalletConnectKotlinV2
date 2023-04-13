package com.walletconnect.android.pairing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.util.generateId

internal sealed class PairingRpc : JsonRpcClientSync<PairingParams> {

    @JsonClass(generateAdapter = true)
    internal data class PairingDelete(
        @Json(name = "id")
        override val id: Long = generateId(),
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
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = PairingJsonRpcMethod.WC_PAIRING_PING,
        @Json(name = "params")
        override val params: PairingParams.PingParams,
    ) : PairingRpc()
}