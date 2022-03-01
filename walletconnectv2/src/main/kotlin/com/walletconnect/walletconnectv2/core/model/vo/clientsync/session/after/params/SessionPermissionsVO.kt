package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.SessionProposedPermissionsVO

@JsonClass(generateAdapter = true)
internal data class SessionPermissionsVO(
    @Json(name = "blockchain")
    val blockchain: SessionProposedPermissionsVO.Blockchain,
    @Json(name = "jsonrpc")
    val jsonRpc: SessionProposedPermissionsVO.JsonRpc,
    @Json(name = "notifications")
    val notifications: SessionProposedPermissionsVO.Notifications?,
)