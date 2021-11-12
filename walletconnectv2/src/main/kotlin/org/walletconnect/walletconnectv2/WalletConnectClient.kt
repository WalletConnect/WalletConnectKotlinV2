package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.common.toClientSessionProposal
import org.walletconnect.walletconnectv2.common.toClientSessionRequest
import org.walletconnect.walletconnectv2.common.toClientSettledSession
import org.walletconnect.walletconnectv2.common.toEngineSessionProposal
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import org.walletconnect.walletconnectv2.engine.sequence.*
import timber.log.Timber

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()
    private var listener: WalletConnectClientListener? = null

    init {
        Timber.plant(Timber.DebugTree())

        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is SequenceLifecycleEvent.OnSessionProposal -> listener?.onSessionProposal(event.proposal.toClientSessionProposal())
                    is SequenceLifecycleEvent.OnSessionSettled -> listener?.onSettledSession(event.session.toClientSettledSession())
                    is SequenceLifecycleEvent.OnSessionRequest -> listener?.onSessionRequest(event.request.toClientSessionRequest())
                    is SequenceLifecycleEvent.OnSessionDeleted -> listener?.onSessionDelete(event.topic, event.reason)
                    else -> SequenceLifecycleEvent.Unsupported
                }
            }
        }
    }

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: pass properties to DI framework
        val engineFactory =
            EngineInteractor
                .EngineFactory(useTls, hostName, apiKey, isController, application, metadata)
        engineInteractor.initialize(engineFactory)
    }

    fun pair(pairingParams: ClientTypes.PairParams, listener: WalletConnectClientListener) {
        this.listener = listener
        //todo handle JsonRpc response
        scope.launch { engineInteractor.pair(pairingParams.uri) }
    }

    fun approve(approveParams: ClientTypes.ApproveParams) = with(approveParams) {
        //todo handle JsonRpc response
        engineInteractor.approve(proposal.toEngineSessionProposal(), accounts)
    }

    fun reject(rejectParams: ClientTypes.RejectParams) = with(rejectParams) {
        //todo handle JsonRpc response
        engineInteractor.reject(rejectionReason, proposalTopic)
    }

    fun disconnect(disconnectParams: ClientTypes.DisconnectParams) = with(disconnectParams) {
        //todo handle JsonRpc response
        engineInteractor.disconnect(topic, reason)
    }
}