package org.walletconnect.walletconnectv2.data.remote

import org.walletconnect.walletconnectv2.data.domain.pairing.Pairing

sealed class WCPairing {
    abstract val id: Int
    abstract val jsonrpc: String
    abstract val method: String
    abstract val params: Pairing

    data class Approve(
        override val id: Int,
        override val jsonrpc: String = "2.0",
        override val method: String = "wc_pairingApprove",
        override val params: Pairing.Success
    ): WCPairing()

    data class Reject(
        override val id: Int,
        override val jsonrpc: String = "2.0",
        override val method: String = "wc_pairingReject",
        override val params: Pairing.Failure
    ): WCPairing()

    companion object {

        @JvmStatic
        fun Pairing.Success.toApprove(): Approve? {
            return null // TODO: create mapping once the difference between expiry and ttl is figured out
        }
    }
}
