@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.pairing.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.android.impl.common.model.RelayProtocolOptions
import com.walletconnect.sign.common.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.utils.DefaultId

internal sealed class PairingParamsVO : ClientParams {

    @JsonClass(generateAdapter = true)
    internal data class SessionProposeParams(
        @Json(name = "relays")
        val relays: List<RelayProtocolOptions>,
        @Json(name = "proposer")
        val proposer: SessionProposerVO,
        @Json(name = "requiredNamespaces")
        val namespaces: Map<String, NamespaceVO.Proposal>,
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