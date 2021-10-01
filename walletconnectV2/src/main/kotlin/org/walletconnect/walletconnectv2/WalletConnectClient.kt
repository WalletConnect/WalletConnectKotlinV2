package org.walletconnect.walletconnectv2

import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.outofband.client.ClientTypes
import org.walletconnect.walletconnectv2.relay.DefaultRelayRepository

object WalletConnectClient {
    private lateinit var relayRepository: DefaultRelayRepository

    fun initialize(useTls: Boolean, hostName: String, port: Int) {
        relayRepository = DefaultRelayRepository.initRemote(hostName = "127.0.0.1")    // replace with remote once starting integration tests
    }

    fun pair(pairingParams: ClientTypes.PairParams) {
        val pairingProposal = pairingParams.uri.toPairProposal()
        val preSettlementPairingApprove = pairingProposal.toApprove(1)

        relayRepository.sendPairingRequest(preSettlementPairingApprove)
    }
}