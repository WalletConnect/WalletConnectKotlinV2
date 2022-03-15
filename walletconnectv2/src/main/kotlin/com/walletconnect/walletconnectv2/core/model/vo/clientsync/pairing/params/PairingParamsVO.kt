package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.BlockchainProposedVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO

internal sealed class PairingParamsVO : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class SessionProposeParams(
        @Json(name = "relays")
        val relays: List<RelayProtocolOptionsVO>,
        @Json(name = "blockchain")
        val blockchainProposedVO: BlockchainProposedVO,
        @Json(name = "permissions")
        val permissions: SessionPermissionsVO,
        @Json(name = "proposer")
        val proposer: SessionProposerVO,
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO,
    ) : PairingParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : PairingParamsVO()
}