package org.walletconnect.walletconnectv2

import org.walletconnect.walletconnectv2.outofband.client.ClientTypes
import org.walletconnect.walletconnectv2.relay.DefaultRelayClient

object WalletConnectClient {
    private lateinit var relayClient: DefaultRelayClient

    init {
        initialize(false, "", 0)
    }

    fun initialize(useTls: Boolean, hostName: String, port: Int) {
        relayClient = DefaultRelayClient.initRemote(hostName = "127.0.0.1")    // replace with remote once starting integration tests
    }

    fun pair(pairParams: ClientTypes.PairParams) {
//        val proposal: Pairing.Proposal = pairParams.uri.toPairProposal()
//        val proposalApproved = proposal.toPairingSuccess()
//        val wcPairingApprove = proposalApproved.toApprove(1)
    }
}