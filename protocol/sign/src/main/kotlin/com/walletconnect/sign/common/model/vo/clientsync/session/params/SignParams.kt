@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.session.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SessionProposer
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.utils.DefaultId

internal sealed class SignParams : CoreSignParams() {

    @JsonClass(generateAdapter = true)
    internal data class SessionProposeParams(
        @Json(name = "requiredNamespaces")
        val requiredNamespaces: Map<String, Namespace.Proposal>,
        @Json(name = "optionalNamespaces")
        val optionalNamespaces: Map<String, Namespace.Proposal>?,
        @Json(name = "relays")
        val relays: List<RelayProtocolOptions>,
        @Json(name = "proposer")
        val proposer: SessionProposer,
        @Json(name = "sessionProperties")
        val properties: Map<String, String>?,
    ) : SignParams()

    @JsonClass(generateAdapter = true)
    internal data class SessionSettleParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptions,
        @Json(name = "controller")
        val controller: SessionParticipantVO,
        @Json(name = "namespaces")
        val namespaces: Map<String, Namespace.Session>,
        @Json(name = "expiry")
        val expiry: Long,
    ) : SignParams()

    @JsonClass(generateAdapter = true)
    internal data class SessionRequestParams(
        @Json(name = "request")
        val request: SessionRequestVO,
        @Json(name = "chainId")
        val chainId: String,
    ) : SignParams()

    internal data class EventParams(
        @Json(name = "event")
        val event: SessionEventVO,
        @Json(name = "chainId")
        val chainId: String,
    ) : SignParams()

    internal class UpdateNamespacesParams(
        @Json(name = "namespaces")
        val namespaces: Map<String, Namespace.Session>,
    ) : SignParams()

    internal data class ExtendParams(
        @Json(name = "expiry")
        val expiry: Long,
    ) : SignParams()

    @JsonClass(generateAdapter = true)
    internal class DeleteParams(
        @Json(name = "code")
        val code: Int = Int.DefaultId,
        @Json(name = "message")
        val message: String,
    ) : SignParams()

    @Suppress("CanSealedSubClassBeObject")
    internal class PingParams : SignParams()
}