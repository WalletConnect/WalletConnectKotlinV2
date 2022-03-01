package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.ProposalRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params.ReasonVO

internal sealed class PairingParamsVO : ClientParams {

    //todo: remove
//    internal data class Proposal(
//        val topic: TopicVO,
//        val relay: RelayProtocolOptionsVO,
//        val proposer: PairingProposerVO,
//        val signal: PairingSignalVO?,
//        val permissions: PairingProposedPermissionsVO?,
//        val ttl: TtlVO
//    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class PayloadParams(
        @Json(name = "request")
        val request: ProposalRequestVO
    ) : PairingParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO
    ) : PairingParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : PairingParamsVO()
}