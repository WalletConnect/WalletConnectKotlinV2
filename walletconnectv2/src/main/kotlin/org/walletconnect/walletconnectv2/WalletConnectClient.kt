package org.walletconnect.walletconnectv2

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.common.toClientSessionProposal
import org.walletconnect.walletconnectv2.common.toClientSessionRequest
import org.walletconnect.walletconnectv2.common.toClientSettledSession
import org.walletconnect.walletconnectv2.common.toEngineSessionProposal
import org.walletconnect.walletconnectv2.common.*
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import org.walletconnect.walletconnectv2.engine.sequence.SequenceLifecycleEvent

object WalletConnectClient {
    private val engineInteractor by lazy { EngineInteractor() }

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: pass properties to DI framework
        app = application
        val engineFactory = EngineInteractor.EngineFactory(useTls, hostName, apiKey, isController, application, metadata)
        engineInteractor.initialize(engineFactory)
    }

    fun setWalletConnectListener(walletConnectListener: WalletConnectClientListener) {
        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is SequenceLifecycleEvent.OnSessionProposal -> walletConnectListener.onSessionProposal(event.proposal.toClientSessionProposal())
                    is SequenceLifecycleEvent.OnSessionRequest -> walletConnectListener.onSessionRequest(event.request.toClientSessionRequest())
                    is SequenceLifecycleEvent.OnSessionDeleted -> walletConnectListener.onSessionDelete(event.deletedSession.toClientDeletedSession())
                }
            }
        }
    }

    fun pair(
        pairingParams: ClientTypes.PairParams,
        listener: WalletConnectClientListeners.Pairing
    ) {
        engineInteractor.pair(pairingParams.uri) { result ->
            result.fold(
                onSuccess = { topic -> listener.onSuccess(WalletConnectClientData.SettledPairing(topic)) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }

    fun approve(
        approveParams: ClientTypes.ApproveParams,
        listener: WalletConnectClientListeners.SessionApprove
    ) = with(approveParams) {
        engineInteractor.approve(proposal.toEngineSessionProposal(), accounts) { result ->
            result.fold(
                onSuccess = { settledSession -> listener.onSuccess(settledSession.toClientSettledSession()) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }

    fun reject(
        rejectParams: ClientTypes.RejectParams,
        listener: WalletConnectClientListeners.SessionReject
    ) = with(rejectParams) {
        engineInteractor.reject(rejectionReason, proposalTopic) { result ->
            result.fold(
                onSuccess = { (topic, reason) -> listener.onSuccess(WalletConnectClientData.RejectedSession(topic, reason)) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }

    fun upgrade(
        upgradeParams: ClientTypes.UpgradeParams,
        listener: WalletConnectClientListeners.SessionUpgrade
    ) = with(upgradeParams) {
        engineInteractor.upgrade(topic, permissions.toEngineSessionPermissions()) { result ->
            result.fold(
                onSuccess = { topic -> listener.onSuccess(WalletConnectClientData.UpgradedSession(topic, permissions)) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }

    fun respond(
        responseParams: ClientTypes.ResponseParams,
        listener: WalletConnectClientListeners.SessionPayload
    ) = with(responseParams) {
        val jsonRpcEngineResponse = when (jsonRpcResponse) {
            is WalletConnectClientData.JsonRpcResponse.JsonRpcResult<*> -> jsonRpcResponse.toEngineRpcResult()
            is WalletConnectClientData.JsonRpcResponse.JsonRpcError -> jsonRpcResponse.toEngineRpcError()
        }
        engineInteractor.respondSessionPayload(sessionTopic, jsonRpcEngineResponse) { result ->
            result.fold(
                onSuccess = { topic -> listener.onSuccess(WalletConnectClientData.Response(topic)) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }

    fun disconnect(
        disconnectParams: ClientTypes.DisconnectParams,
        listener: WalletConnectClientListeners.SessionDelete
    ) = with(disconnectParams) {
        engineInteractor.disconnect(sessionTopic, reason) { result ->
            result.fold(
                onSuccess = { (topic, reason) -> listener.onSuccess(WalletConnectClientData.DeletedSession(topic, reason)) },
                onFailure = { error -> listener.onError(error) }
            )
        }
    }
}