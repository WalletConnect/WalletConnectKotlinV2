package com.walletconnect.walletconnectv2.client.presentation

import com.walletconnect.walletconnectv2.client.model.*
import com.walletconnect.walletconnectv2.common.app
import com.walletconnect.walletconnectv2.common.scope
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineData
import com.walletconnect.walletconnectv2.engine.model.SequenceLifecycle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: pass properties to DI framework
        app = application
        val engineFactory = EngineInteractor.EngineFactory(useTls, hostName, projectId, isController, application, metadata)
        engineInteractor.initialize(engineFactory)
    }

    fun setWalletConnectListener(walletConnectListener: WalletConnectClientListener) {
        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is SequenceLifecycle.OnSessionProposal -> walletConnectListener.onSessionProposal(event.proposal.toClientSessionProposal())
                    is SequenceLifecycle.OnSessionRequest -> walletConnectListener.onSessionRequest(event.request.toClientSessionRequest())
                    is SequenceLifecycle.OnSessionDeleted -> walletConnectListener.onSessionDelete(event.deletedSession.toClientDeletedSession())
                    is SequenceLifecycle.OnSessionNotification -> walletConnectListener.onSessionNotification(event.notification.toClientSessionNotification())
                    SequenceLifecycle.Default -> Unit
                }
            }
        }
    }

    fun pair(
        pairingParams: ClientTypes.PairParams,
        listener: WalletConnectClientListeners.Pairing
    ) {
        engineInteractor.pair(
            pairingParams.uri,
            { topic -> listener.onSuccess(WalletConnectClientData.SettledPairing(topic)) },
            { error -> listener.onError(error) })
    }

    fun approve(
        approveParams: ClientTypes.ApproveParams,
        listener: WalletConnectClientListeners.SessionApprove
    ) = with(approveParams) {
        engineInteractor.approve(
            proposal.toEngineSessionProposal(accounts),
            { settledSession -> listener.onSuccess(settledSession.toClientSettledSession()) },
            { error -> listener.onError(error) })
    }

    fun reject(
        rejectParams: ClientTypes.RejectParams,
        listener: WalletConnectClientListeners.SessionReject
    ) = with(rejectParams) {
        engineInteractor.reject(
            rejectionReason, proposalTopic,
            { (topic, reason) -> listener.onSuccess(WalletConnectClientData.RejectedSession(topic, reason)) },
            { error -> listener.onError(error) })
    }

    fun respond(
        responseParams: ClientTypes.ResponseParams,
        listener: WalletConnectClientListeners.SessionPayload
    ) = with(responseParams) {
        val jsonRpcEngineResponse = when (jsonRpcResponse) {
            is WalletConnectClientData.JsonRpcResponse.JsonRpcResult -> jsonRpcResponse.toEngineRpcResult()
            is WalletConnectClientData.JsonRpcResponse.JsonRpcError -> jsonRpcResponse.toEngineRpcError()
        }
        engineInteractor.respondSessionPayload(sessionTopic, jsonRpcEngineResponse) { error -> listener.onError(error) }
    }

    fun upgrade(
        upgradeParams: ClientTypes.UpgradeParams,
        listener: WalletConnectClientListeners.SessionUpgrade
    ) = with(upgradeParams) {
        engineInteractor.upgrade(
            topic, permissions.toEngineSessionPermissions(),
            { (topic, permissions) -> listener.onSuccess(WalletConnectClientData.UpgradedSession(topic, permissions.toClientPerms())) },
            { error -> listener.onError(error) })
    }

    fun update(
        updateParams: ClientTypes.UpdateParams,
        listener: WalletConnectClientListeners.SessionUpdate
    ) = with(updateParams) {
        engineInteractor.update(
            sessionTopic, sessionState.toEngineSessionState(),
            { (topic, accounts) -> listener.onSuccess(WalletConnectClientData.UpdatedSession(topic, accounts)) },
            { error -> listener.onError(error) })
    }

    fun ping(
        pingParams: ClientTypes.PingParams,
        listener: WalletConnectClientListeners.SessionPing
    ) {
        engineInteractor.ping(pingParams.topic,
            { topic -> listener.onSuccess(topic) },
            { error -> listener.onError(error) })
    }

    fun notify(
        notificationParams: ClientTypes.NotificationParams,
        listener: WalletConnectClientListeners.Notification
    ) = with(notificationParams) {
        engineInteractor.notify(topic, notification.toEngineNotification(),
            { topic -> listener.onSuccess(topic) },
            { error -> listener.onError(error) })
    }

    fun disconnect(
        disconnectParams: ClientTypes.DisconnectParams,
        listener: WalletConnectClientListeners.SessionDelete
    ) = with(disconnectParams) {
        engineInteractor.disconnect(
            sessionTopic, reason,
            { (topic, reason) -> listener.onSuccess(WalletConnectClientData.DeletedSession(topic, reason)) },
            { error -> listener.onError(error) })
    }

    fun getListOfSettledSessions(): List<WalletConnectClientData.SettledSession> =
        engineInteractor.getListOfSettledSessions().map(EngineData.SettledSession::toClientSettledSession)

    fun getListOfPendingSession(): List<WalletConnectClientData.SessionProposal> =
        engineInteractor.getListOfPendingSessions().map(EngineData.SessionProposalDO::toClientSessionProposal)

    fun shutdown() {
        scope.cancel()
    }
}