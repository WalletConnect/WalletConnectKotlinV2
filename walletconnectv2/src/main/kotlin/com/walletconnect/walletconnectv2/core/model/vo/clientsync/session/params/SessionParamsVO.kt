package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.ReasonVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.BlockchainSettledVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.SessionEventVO
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
        val permission: SessionPermissionsVO, //todo: flatten permissions structure
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
        val chainId: String?,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "reason")
        val reason: ReasonVO,
    ) : SessionParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : SessionParamsVO()

    internal data class EventParams(
        @Json(name = "event")
        val event: SessionEventVO,
        @Json(name = "chainId")
        val chainId: String?,
    ) : SessionParamsVO()

    internal class UpdateEventsParams(
        @Json(name = "events")
        val events: List<String>,
    ) : SessionParamsVO()

    internal class UpdateAccountsParams(
        @Json(name = "accounts")
        val accounts: List<String>,
    ) : SessionParamsVO()

    internal class UpdateMethodsParams(
        @Json(name = "methods")
        val methods: List<String>,
    ) : SessionParamsVO()

    internal data class UpdateExpiryParams(
        @Json(name = "expiry")
        val expiry: Long,
    ) : SessionParamsVO()
}