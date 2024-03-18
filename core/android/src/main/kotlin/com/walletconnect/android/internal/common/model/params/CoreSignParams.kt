package com.walletconnect.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.Participant
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.signing.cacao.Cacao

open class CoreSignParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class ApprovalParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptions,
        @Json(name = "responderPublicKey")
        val responderPublicKey: String,
    ) : CoreSignParams()

    @JsonClass(generateAdapter = true)
    data class SessionAuthenticateApproveParams(
        @Json(name = "responder")
        val responder: Participant,
        @Json(name = "cacaos")
        val cacaos: List<Cacao>,
    ) : CoreSignParams()
}