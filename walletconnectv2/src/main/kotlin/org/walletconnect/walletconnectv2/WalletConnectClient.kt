package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.SessionProposal
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import timber.log.Timber
import java.net.URI

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()
    private var pairingListener: WalletConnectClientListeners.Pairing? = null

    init {
        Timber.plant(Timber.DebugTree())

        scope.launch {
            engineInteractor.sessionProposal.collect { proposal ->
                proposal?.toSessionProposal()?.let { sessionProposal ->
                    pairingListener?.onSessionProposal(sessionProposal)
                }
            }
        }
    }

    fun initialize(initialParams: ClientTypes.InitialParams) {
        // TODO: pass properties to DI framework
        val engineFactory = EngineInteractor.EngineFactory(
            useTLs = initialParams.useTls,
            hostName = initialParams.hostName,
            apiKey = initialParams.apiKey,
            isController = initialParams.isController,
            application = initialParams.application,
            metaData = initialParams.metadata
        )
        engineInteractor.initialize(engineFactory)
    }

    fun pair(
        pairingParams: ClientTypes.PairParams,
        clientListeners: WalletConnectClientListeners.Pairing
    ) {
        pairingListener = clientListeners
        scope.launch {
            engineInteractor.pair(pairingParams.uri)
        }
    }

    fun approve(approveParams: ClientTypes.ApproveParams) {
        engineInteractor.approve(
            approveParams.accounts,
            approveParams.proposerPublicKey,
            approveParams.proposalTtl,
            approveParams.proposalTopic
        )
    }

    fun reject(rejectParams: ClientTypes.RejectParams) {
        engineInteractor.reject(rejectParams.rejectionReason, rejectParams.proposalTopic)
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