package com.walletconnect.android.internal

internal object JsonRpcMethod {
    @get:JvmSynthetic
    const val WC_PAIRING_PING: String = "wc_pairingPing"
    @get:JvmSynthetic
    const val WC_PAIRING_DELETE: String = "wc_pairingDelete"
    @get:JvmSynthetic
    const val WC_SESSION_PROPOSE: String = "wc_sessionPropose"
}