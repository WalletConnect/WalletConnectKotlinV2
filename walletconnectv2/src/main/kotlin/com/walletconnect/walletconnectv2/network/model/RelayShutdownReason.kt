package com.walletconnect.walletconnectv2.network.model

import com.tinder.scarlet.ShutdownReason

internal data class RelayShutdownReason(val code: Int, val reason: String) {
    companion object {
        private const val NORMAL_CLOSURE_STATUS_CODE = 1000
        private const val NORMAL_CLOSURE_REASON = "Normal closure"

        @JvmField
        val GRACEFUL = RelayShutdownReason(NORMAL_CLOSURE_STATUS_CODE, NORMAL_CLOSURE_REASON)
    }
}

internal fun ShutdownReason.toRelayShutdownReason() = RelayShutdownReason(code, reason)