package com.walletconnect.sign.json_rpc.model

internal object JsonRpcMethod {
    @get:JvmSynthetic
    const val WC_PAIRING_PING: String = "wc_pairingPing"
    @get:JvmSynthetic
    const val WC_PAIRING_DELETE: String = "wc_pairingDelete"
    @get:JvmSynthetic
    const val WC_SESSION_PROPOSE: String = "wc_sessionPropose"

    @get:JvmSynthetic
    const val WC_SESSION_SETTLE: String = "wc_sessionSettle"
    @get:JvmSynthetic
    const val WC_SESSION_REQUEST: String = "wc_sessionRequest"
    @get:JvmSynthetic
    const val WC_SESSION_DELETE: String = "wc_sessionDelete"
    @get:JvmSynthetic
    const val WC_SESSION_PING: String = "wc_sessionPing"
    @get:JvmSynthetic
    const val WC_SESSION_EVENT: String = "wc_sessionEvent"
    @get:JvmSynthetic
    const val WC_SESSION_UPDATE: String = "wc_sessionUpdate"
    @get:JvmSynthetic
    const val WC_SESSION_EXTEND: String = "wc_sessionExtend"
}