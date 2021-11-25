package org.walletconnect.walletconnectv2.clientsync.session.after.params

import com.squareup.moshi.Json
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposedPermissions

data class SessionPermissions(
    @Json(name = "blockchain")
    val blockchain: SessionProposedPermissions.Blockchain? = null,
    @Json(name = "jsonrpc")
    val jsonRpc: SessionProposedPermissions.JsonRpc? = null,
)
