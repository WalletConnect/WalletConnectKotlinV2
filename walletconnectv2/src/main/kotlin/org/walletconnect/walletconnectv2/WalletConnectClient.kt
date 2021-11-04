package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.common.toSessionProposal
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import timber.log.Timber

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

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: pass properties to DI framework
        val engineFactory =
            EngineInteractor.EngineFactory(
                useTls, hostName, apiKey, isController, application, metadata
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

    fun approve(approveParams: ClientTypes.ApproveParams) = with(approveParams) {
        engineInteractor.approve(accounts, proposerPublicKey, proposalTtl, proposalTopic)
    }

    fun reject(rejectParams: ClientTypes.RejectParams) = with(rejectParams) {
        engineInteractor.reject(rejectionReason, proposalTopic)
    }
}