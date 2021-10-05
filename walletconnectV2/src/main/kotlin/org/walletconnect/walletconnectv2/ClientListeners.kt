package org.walletconnect.walletconnectv2

sealed interface ClientListeners {

    fun interface Pairing: ClientListeners {

        fun pairingResponse(settledTopic: String)
    }
}
