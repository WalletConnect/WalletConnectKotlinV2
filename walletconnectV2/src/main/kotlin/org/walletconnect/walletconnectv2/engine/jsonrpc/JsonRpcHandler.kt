package org.walletconnect.walletconnectv2.engine.jsonrpc

import org.walletconnect.walletconnectv2.clientsync.session.Session

interface JsonRpcHandler {
    var onSessionPropose: (proposal: Session.Proposal) -> Unit
    var onSessionRequest: (payload: Any) -> Unit
    var onSessionDelete: () -> Unit
    var onUnsupported: (rpc: String?) -> Unit
}