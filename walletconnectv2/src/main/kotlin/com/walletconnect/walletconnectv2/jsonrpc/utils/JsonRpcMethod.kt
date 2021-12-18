package com.walletconnect.walletconnectv2.jsonrpc.utils

object JsonRpcMethod {
    const val WC_PAIRING_PAYLOAD: String = "wc_pairingPayload"
    const val WC_PAIRING_APPROVE: String = "wc_pairingApprove"
    const val WC_PAIRING_REJECT: String = "wc_pairingReject"
    const val WC_PAIRING_UPDATE: String = "wc_pairingUpdate"
    const val WC_PAIRING_PING: String = "wc_pairingPing"
    const val WC_PAIRING_DELETE: String = "wc_pairingDelete"
    const val WC_PAIRING_NOTIFICATION: String = "wc_pairingNotification"

    const val WC_SESSION_PAYLOAD: String = "wc_sessionPayload"
    const val WC_SESSION_PROPOSE: String = "wc_sessionPropose"
    const val WC_SESSION_APPROVE: String = "wc_sessionApprove"
    const val WC_SESSION_UPDATE: String = "wc_sessionUpdate"
    const val WC_SESSION_UPGRADE: String = "wc_sessionUpgrade"
    const val WC_SESSION_REJECT: String = "wc_sessionReject"
    const val WC_SESSION_DELETE: String = "wc_sessionDelete"
    const val WC_SESSION_PING: String = "wc_sessionPing"
    const val WC_SESSION_NOTIFICATION: String = "wc_sessionNotification"
}