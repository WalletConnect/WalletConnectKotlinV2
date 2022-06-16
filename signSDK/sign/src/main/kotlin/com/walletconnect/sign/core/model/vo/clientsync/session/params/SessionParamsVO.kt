package com.walletconnect.sign.core.model.vo.clientsync.session.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.sign.core.model.type.ClientParams
import com.walletconnect.sign.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.util.DefaultId

internal sealed class SessionParamsVO : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class ApprovalParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "responderPublicKey")
        val responderPublicKey: String,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionSettleParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptionsVO,
        @Json(name = "controller")
        val controller: SessionParticipantVO,
        @Json(name = "namespaces")
        val namespaces: Map<String, NamespaceVO.Session>,
        @Json(name = "expiry")
        val expiry: Long,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal data class SessionRequestParams(
        @Json(name = "request")
        val request: SessionRequestVO,
        @Json(name = "chainId")
        val chainId: String,
    ) : SessionParamsVO()

    internal data class EventParams(
        @Json(name = "event")
        val event: SessionEventVO,
        @Json(name = "chainId")
        val chainId: String,
    ) : SessionParamsVO()

    internal class UpdateNamespacesParams(
        @Json(name = "namespaces")
        val namespaces: Map<String, NamespaceVO.Session>,
    ) : SessionParamsVO()

    internal data class ExtendParams(
        @Json(name = "expiry")
        val expiry: Long,
    ) : SessionParamsVO()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "code")
        val code: Int = Int.DefaultId,
        @Json(name = "message")
        val message: String,
    ) : SessionParamsVO()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : SessionParamsVO()
}