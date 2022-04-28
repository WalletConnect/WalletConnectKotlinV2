package com.walletconnect.walletconnectv2.core.model.utils

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
    const val WC_SESSION_UPDATE_ACCOUNTS: String = "wc_sessionUpdateAccounts"
    @get:JvmSynthetic
    const val WC_SESSION_UPDATE_NAMESPACES: String = "wc_sessionUpdateNamespaces"
    @get:JvmSynthetic
    const val WC_SESSION_UPDATE_EXPIRY: String = "wc_sessionUpdateExpiry"
}