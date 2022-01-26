package com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO

@JsonClass(generateAdapter = true)
internal data class PairingStateVO(
    @Json(name = "metadata")
    val metadata: AppMetaDataVO
)
