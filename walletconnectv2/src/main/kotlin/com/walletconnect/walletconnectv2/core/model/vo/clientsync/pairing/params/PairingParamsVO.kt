package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.walletconnectv2.util.DefaultId

internal sealed class PairingParamsVO : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class SessionProposeParams(
        @Json(name = "relays")
        val relays: List<RelayProtocolOptionsVO>,
        @Json(name = "proposer")
        val proposer: SessionProposerVO,
        @Json(name = "chains")
        val chains: List<String>,
        @Json(name = "methods")
        val methods: List<String>,
        @Json(name = "events")
        val events: List<String>,
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "code")
        val code: Int = Int.DefaultId,
        @Json(name = "message")
        val message: String,
    ) : PairingParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : PairingParamsVO()
}