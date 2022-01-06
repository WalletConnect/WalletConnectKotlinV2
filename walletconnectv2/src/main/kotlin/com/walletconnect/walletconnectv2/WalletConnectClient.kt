package com.walletconnect.walletconnectv2

import com.walletconnect.walletconnectv2.client.ClientTypes
import com.walletconnect.walletconnectv2.client.WalletConnectClientData
import com.walletconnect.walletconnectv2.client.WalletConnectClientListener
import com.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import com.walletconnect.walletconnectv2.common.*
import com.walletconnect.walletconnectv2.di.DaggerWCComponent
import com.walletconnect.walletconnectv2.engine.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineData
import com.walletconnect.walletconnectv2.engine.sequence.SequenceLifecycle
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object WalletConnectClient {
    private lateinit var engineInteractor: EngineInteractor

    fun initialize(initialParams: ClientTypes.InitialParams) = with(initialParams) {
        // TODO: add logic to check hostName for ws/wss scheme with and without ://

        engineInteractor = DaggerWCComponent.builder()
            .useTls(useTls)
            .hostName(hostName)
            .projectID(projectId)
            .application(application)
            .create()
            .engineFactory()
            .create(isController, metadata)
    }

    fun setWalletConnectListener(walletConnectListener: WalletConnectClientListener) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

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
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        engineInteractor.pair(
            pairingParams.uri,
            { topic -> listener.onSuccess(WalletConnectClientData.SettledPairing(topic)) },
            { error -> listener.onError(error) })
    }

    fun approve(
        approveParams: ClientTypes.ApproveParams,
        listener: WalletConnectClientListeners.SessionApprove
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(approveParams) {
            engineInteractor.approve(
                proposal.toEngineSessionProposal(accounts),
                { settledSession -> listener.onSuccess(settledSession.toClientSettledSession()) },
                { error -> listener.onError(error) })
        }
    }

    fun reject(
        rejectParams: ClientTypes.RejectParams,
        listener: WalletConnectClientListeners.SessionReject
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(rejectParams) {
            engineInteractor.reject(
                rejectionReason, proposalTopic,
                { (topic, reason) -> listener.onSuccess(WalletConnectClientData.RejectedSession(topic, reason)) },
                { error -> listener.onError(error) })
        }
    }

    fun respond(
        responseParams: ClientTypes.ResponseParams,
        listener: WalletConnectClientListeners.SessionPayload
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(responseParams) {
            val jsonRpcEngineResponse = when (jsonRpcResponse) {
                is WalletConnectClientData.JsonRpcResponse.JsonRpcResult -> jsonRpcResponse.toEngineRpcResult()
                is WalletConnectClientData.JsonRpcResponse.JsonRpcError -> jsonRpcResponse.toEngineRpcError()
            }
            engineInteractor.respondSessionPayload(sessionTopic, jsonRpcEngineResponse) { error -> listener.onError(error) }
        }
    }

    fun upgrade(
        upgradeParams: ClientTypes.UpgradeParams,
        listener: WalletConnectClientListeners.SessionUpgrade
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(upgradeParams) {
            engineInteractor.upgrade(
                topic, permissions.toEngineSessionPermissions(),
                { (topic, permissions) -> listener.onSuccess(WalletConnectClientData.UpgradedSession(topic, permissions.toClientPerms())) },
                { error -> listener.onError(error) })
        }
    }

    fun update(
        updateParams: ClientTypes.UpdateParams,
        listener: WalletConnectClientListeners.SessionUpdate
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(updateParams) {
            engineInteractor.update(
                sessionTopic, sessionState.toEngineSessionState(),
                { (topic, accounts) -> listener.onSuccess(WalletConnectClientData.UpdatedSession(topic, accounts)) },
                { error -> listener.onError(error) })
        }
    }

    fun ping(
        pingParams: ClientTypes.PingParams,
        listener: WalletConnectClientListeners.SessionPing
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        engineInteractor.ping(pingParams.topic,
            { topic -> listener.onSuccess(topic) },
            { error -> listener.onError(error) })
    }

    fun notify(
        notificationParams: ClientTypes.NotificationParams,
        listener: WalletConnectClientListeners.Notification
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(notificationParams) {
            engineInteractor.notify(topic, notification.toEngineNotification(),
                { topic -> listener.onSuccess(topic) },
                { error -> listener.onError(error) })
        }
    }

    fun disconnect(
        disconnectParams: ClientTypes.DisconnectParams,
        listener: WalletConnectClientListeners.SessionDelete
    ) {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        with(disconnectParams) {
            engineInteractor.disconnect(
                sessionTopic, reason,
                { (topic, reason) -> listener.onSuccess(WalletConnectClientData.DeletedSession(topic, reason)) },
                { error -> listener.onError(error) })
        }
    }

    fun getListOfSettledSessions(): List<WalletConnectClientData.SettledSession> {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        return engineInteractor.getListOfSettledSessions().map(EngineData.SettledSession::toClientSettledSession)
    }

    fun getListOfPendingSession(): List<WalletConnectClientData.SessionProposal> {
        require(::engineInteractor.isInitialized) {
            "WalletConnectClient.initialize has not been called yet"
        }

        return engineInteractor.getListOfPendingSessions().map(EngineData.SessionProposal::toClientSessionProposal)
    }

    fun shutdown() {
        scope.cancel()
    }
}

