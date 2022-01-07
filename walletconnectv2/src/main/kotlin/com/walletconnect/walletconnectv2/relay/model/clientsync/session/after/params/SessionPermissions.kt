package com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params

import com.squareup.moshi.Json
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.SessionProposedPermissions

internal data class SessionPermissions(
    @Json(name = "blockchain")
    val blockchain: SessionProposedPermissions.Blockchain? = null,
    @Json(name = "jsonrpc")
    val jsonRpc: SessionProposedPermissions.JsonRpc? = null,
)
