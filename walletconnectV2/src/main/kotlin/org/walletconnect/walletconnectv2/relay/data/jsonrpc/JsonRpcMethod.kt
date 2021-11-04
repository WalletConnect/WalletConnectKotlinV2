package org.walletconnect.walletconnectv2.relay.data.jsonrpc

object JsonRpcMethod {
    const val wcPairingPayload: String = "wc_pairingPayload"
    const val wcPairingApprove: String = "wc_pairingApprove"
    const val wcPairingReject: String = "wc_pairingReject"

    const val wcSessionPayload: String = "wc_sessionPayload"
    const val wcSessionPropose: String = "wc_sessionPropose"
    const val wcSessionApprove: String = "wc_sessionApprove"
    const val wcSessionReject: String = "wc_sessionReject"
    const val wcSessionDelete: String = "wc_sessionDelete"
}