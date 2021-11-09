package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import org.walletconnect.walletconnectv2.engine.jsonrpc.JsonRpcEvent

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()
    private var pairingListener: WalletConnectClientListeners.Pairing? = null
    private var sessionListener: WalletConnectClientListeners.Session? = null

    init {
        scope.launch {
            engineInteractor.jsonRpcEvents.collect { event ->
                when (event) {
                    is JsonRpcEvent.OnSessionProposal -> pairingListener?.onSessionProposal(event.proposal)
                    is JsonRpcEvent.OnSessionRequest -> sessionListener?.onSessionRequest(event.payload)
                    else -> JsonRpcEvent.Unsupported
                }
            }
        }
    }

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: pass properties to DI framework
        val engineFactory = EngineInteractor.EngineFactory(useTls, hostName, apiKey, isController, application, metadata)
        engineInteractor.initialize(engineFactory)
    }

    fun pair(
        pairingParams: ClientTypes.PairParams,
        clientListeners: WalletConnectClientListeners.Pairing
    ) {
        pairingListener = clientListeners
        engineInteractor.pair(pairingParams.uri)
    }

    fun approve(
        approveParams: ClientTypes.ApproveParams,
        sessionRequestListener: WalletConnectClientListeners.Session
    ) = with(approveParams) {
        sessionListener = sessionRequestListener
        engineInteractor.approve(accounts, proposerPublicKey, proposalTtl, proposalTopic)
    }

    fun reject(rejectParams: ClientTypes.RejectParams) = with(rejectParams) {
        engineInteractor.reject(rejectionReason, proposalTopic)
    }

    // TODO: Add close method to cancel coroutine scope
}