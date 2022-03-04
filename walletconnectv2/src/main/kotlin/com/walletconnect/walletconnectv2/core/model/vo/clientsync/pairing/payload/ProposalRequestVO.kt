package com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionParamsVO

@JsonClass(generateAdapter = true)
internal data class ProposalRequestVO(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: SessionParamsVO.ProposalParams
)