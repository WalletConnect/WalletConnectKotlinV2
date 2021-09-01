package org.walletconnect.walletconnectv2

import org.walletconnect.walletconnectv2.data.domain.ClientTypes
import org.walletconnect.walletconnectv2.data.domain.pairing.Pairing.Companion.toPairProposal

object WalletConnectClient {
    private lateinit var webSocketClient: DefaultWebSocketClient

    init {
        initialize(false, "", 0)
    }

    fun initialize(useTls: Boolean, hostName: String, port: Int) {
        webSocketClient = DefaultWebSocketClient.initLocal()    // replace with remote once starting integration tests
    }

    fun pair(pairParams: ClientTypes.PairParams) {
        val proposal = pairParams.uri.toPairProposal()

    }
}