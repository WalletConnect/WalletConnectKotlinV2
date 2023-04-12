package com.walletconnect.sync.common.json_rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

internal sealed class SyncParams : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class SetParams(
        @Json(name = "key")
        val key: String,
        @Json(name = "value")
        val value: String,
    ) : SyncParams()

    @JsonClass(generateAdapter = true)
    internal data class DeleteParams(
        @Json(name = "key")
        val key: String,
    ) : SyncParams()
}
