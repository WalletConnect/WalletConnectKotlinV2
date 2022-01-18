package com.walletconnect.walletconnectv2.relay.model.utils

internal object JsonRpcMethod {
    @get:JvmSynthetic const val WC_PAIRING_PAYLOAD: String = "wc_pairingPayload"
    @get:JvmSynthetic const val WC_PAIRING_APPROVE: String = "wc_pairingApprove"
    @get:JvmSynthetic const val WC_PAIRING_REJECT: String = "wc_pairingReject"
    @get:JvmSynthetic const val WC_PAIRING_UPDATE: String = "wc_pairingUpdate"
    @get:JvmSynthetic const val WC_PAIRING_PING: String = "wc_pairingPing"
    @get:JvmSynthetic const val WC_PAIRING_DELETE: String = "wc_pairingDelete"
    @get:JvmSynthetic const val WC_PAIRING_NOTIFICATION: String = "wc_pairingNotification"

    @get:JvmSynthetic const val WC_SESSION_PAYLOAD: String = "wc_sessionPayload"
    @get:JvmSynthetic const val WC_SESSION_PROPOSE: String = "wc_sessionPropose"
    @get:JvmSynthetic const val WC_SESSION_APPROVE: String = "wc_sessionApprove"
    @get:JvmSynthetic const val WC_SESSION_UPDATE: String = "wc_sessionUpdate"
    @get:JvmSynthetic const val WC_SESSION_UPGRADE: String = "wc_sessionUpgrade"
    @get:JvmSynthetic const val WC_SESSION_REJECT: String = "wc_sessionReject"
    @get:JvmSynthetic const val WC_SESSION_DELETE: String = "wc_sessionDelete"
    @get:JvmSynthetic const val WC_SESSION_PING: String = "wc_sessionPing"
    @get:JvmSynthetic const val WC_SESSION_NOTIFICATION: String = "wc_sessionNotification"
}