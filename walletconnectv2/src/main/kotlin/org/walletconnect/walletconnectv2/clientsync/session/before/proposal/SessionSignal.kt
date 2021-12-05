package org.walletconnect.walletconnectv2.clientsync.session.before.proposal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter

@JsonClass(generateAdapter = true)
data class SessionSignal(
    @Json(name = "method")
    val method: String = "pairing",
    @Json(name = "params")
    val params: Params
) {

    data class Params(
        @field:TopicAdapter.Qualifier
        val topic: Topic
    )
}