package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params

import com.squareup.moshi.Json
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.SessionProposedPermissionsVO

internal data class SessionPermissionsVO(
    @Json(name = "blockchain")
    val blockchain: SessionProposedPermissionsVO.Blockchain? = null,
    @Json(name = "jsonrpc")
    val jsonRpc: SessionProposedPermissionsVO.JsonRpc? = null,
)
