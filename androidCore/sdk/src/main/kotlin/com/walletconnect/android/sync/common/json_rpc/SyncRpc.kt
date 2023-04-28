package com.walletconnect.android.sync.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.util.generateId

internal sealed class SyncRpc : JsonRpcClientSync<SyncParams> {
    abstract override val id: Long
    abstract override val method: String
    abstract override val jsonrpc: String
    abstract override val params: SyncParams

    @JsonClass(generateAdapter = true)
    internal data class SyncSet(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SYNC_SET,
        @Json(name = "params")
        override val params: SyncParams.SetParams,
    ) : SyncRpc()

    @JsonClass(generateAdapter = true)
    internal data class SyncDelete(
        @Json(name = "id")
        override val id: Long = generateId(),
        @Json(name = "jsonrpc")
        override val jsonrpc: String = "2.0",
        @Json(name = "method")
        override val method: String = JsonRpcMethod.WC_SYNC_DELETE,
        @Json(name = "params")
        override val params: SyncParams.DeleteParams,
    ) : SyncRpc()

}