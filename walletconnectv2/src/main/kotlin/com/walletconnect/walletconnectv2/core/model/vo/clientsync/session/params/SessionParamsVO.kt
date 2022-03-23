package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.BlockchainSettledVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.SessionRequestVO

internal sealed class SessionParamsVO : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class ApprovalParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "responder")
        val responder: SessionParticipantVO,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionSettleParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "blockchain")
        val blockchain: BlockchainSettledVO,
        @Json(name = "permissions")
        val permission: SessionPermissionsVO,
        @Json(name = "controller")
        val controller: SessionParticipantVO,
        @Json(name = "expiry")
        val expiry: Long,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionRequestParams(
        @Json(name = "request")
        val request: SessionRequestVO,
        @Json(name = "chainId")
        val chainId: String?
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO
    ) : SessionParamsVO()

    internal class UpdateParams(
        @Json(name = "blockchain")
        val blockchain: BlockchainSettledVO
    ) : SessionParamsVO()

    internal data class UpgradeParams(
        @Json(name = "permissions")
        val permissions: SessionPermissionsVO
    ) : SessionParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : SessionParamsVO()

    internal data class NotifyParams(
        @Json(name = "type")
        val type: String,
        @Json(name = "data")
        val data: Any
    ) : SessionParamsVO()

    internal data class ExtendParams(
        @Json(name = "expiry")
        val expiry: Long,
    ) : SessionParamsVO()
}