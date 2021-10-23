package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.WalletConnectScope.scope
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.clientcomm.session.Session
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import java.net.URI

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()
    private var pairingListener: WalletConnectClientListeners.Pairing? = null

    init {
        scope.launch {
            engineInteractor.sessionProposal.collect {
                it?.toSessionProposal()
                    ?.let { sessionProposal -> pairingListener?.onSessionProposal(sessionProposal) }
            }
        }
    }

    fun initialize(initialParams: ClientTypes.InitialParams) {
        // TODO: pass properties to DI framework
        val engineFactory = EngineInteractor.EngineFactory(
            useTLs = initialParams.useTls,
            hostName = initialParams.hostName,
            application = initialParams.application
        )
        engineInteractor.initialize(engineFactory)
    }

    fun pair(
        pairingParams: ClientTypes.PairParams,
        clientListeners: WalletConnectClientListeners.Pairing
    ) {
        pairingListener = clientListeners
        scope.launch { engineInteractor.pair(pairingParams.uri) }
    }

    fun approve(proposal: SessionProposal) {
        engineInteractor.approve(proposal)
    }

    private fun Session.Proposal.toSessionProposal(): SessionProposal {
        return SessionProposal(
            name = this.proposer.metadata?.name!!,
            description = this.proposer.metadata.description,
            dappUrl = this.proposer.metadata.url,
            icon = this.proposer.metadata.icons.map { URI(it) },
            chains = this.permissions.blockchain.chains,
            methods = this.permissions.jsonRpc.methods,
            topic = this.topic.topicValue,
            proposerPublicKey = this.proposer.publicKey,
            ttl = this.ttl.seconds
        )
    }
}