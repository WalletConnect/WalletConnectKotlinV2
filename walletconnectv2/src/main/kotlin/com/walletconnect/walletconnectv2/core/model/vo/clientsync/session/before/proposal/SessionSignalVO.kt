package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO

@JsonClass(generateAdapter = true)
internal data class SessionSignalVO(
    @Json(name = "method")
    val method: String = "pairing",
    @Json(name = "params")
    val params: Params
) {

    internal data class Params(
        @field:TopicAdapter.Qualifier
        val topic: TopicVO
    )
}