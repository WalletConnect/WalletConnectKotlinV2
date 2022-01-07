package com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.adapters.TopicAdapter

@JsonClass(generateAdapter = true)
internal data class SessionSignal(
    @Json(name = "method")
    val method: String = "pairing",
    @Json(name = "params")
    val params: Params
) {

    data class Params(
        @field:TopicAdapter.Qualifier
        val topic: TopicVO
    )
}