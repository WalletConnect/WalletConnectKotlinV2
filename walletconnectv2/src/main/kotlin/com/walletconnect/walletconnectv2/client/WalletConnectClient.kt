package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.client.mapper.toClientSessionProposal
import com.walletconnect.walletconnectv2.client.mapper.toClientSessionRequest
import com.walletconnect.walletconnectv2.client.mapper.toClientSettledSession
import com.walletconnect.walletconnectv2.client.mapper.toEngineSessionProposal
import com.walletconnect.walletconnectv2.common.scope.app
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object WalletConnectClient {
    private val engineInteractor = EngineInteractor()

    fun setDelegate(delegate: Delegate) {
        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is EngineDO.SessionProposal -> delegate.onSessionProposal(event.toClientSessionProposal())
                    is EngineDO.SessionRequest -> delegate.onSessionRequest(event.toClientSessionRequest())
                    is EngineDO.DeletedSession -> delegate.onSessionDelete(event.toClientDeletedSession())
                    is EngineDO.SessionNotification -> delegate.onSessionNotification(event.toClientSessionNotification())
                }
            }
        }
    }

    fun initialize(initial: Init) = with(initial) {
        // TODO: pass properties to DI framework
        app = application
        val engineFactory =
            EngineInteractor.EngineFactory(useTls, hostName, projectId, isController, application, metadata.toEngineAppMetaData())
        engineInteractor.initialize(engineFactory)
    }

    fun pair(pair: Pair, pairing: Pairing) {
        engineInteractor.pair(pair.uri,
            { topic -> pairing.onSuccess(SettledPairing(topic)) },
            { error -> pairing.onError(error) })
    }

    fun approve(approve: Approve, sessionApprove: SessionApprove) = with(approve) {
        engineInteractor.approve(
            proposal.toEngineSessionProposal(accounts),
            { settledSession -> sessionApprove.onSuccess(settledSession.toClientSettledSession()) },
            { error -> sessionApprove.onError(error) })
    }

    fun reject(reject: Reject, sessionReject: SessionReject) = with(reject) {
        engineInteractor.reject(
            rejectionReason, proposalTopic,
            { (topic, reason) -> sessionReject.onSuccess(RejectedSession(topic, reason)) },
            { error -> sessionReject.onError(error) })
    }

    fun respond(response: Response, sessionPayload: SessionPayload) = with(response) {
        engineInteractor.respondSessionPayload(sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO())
        { error -> sessionPayload.onError(error) }
    }

    fun upgrade(upgrade: Upgrade, sessionUpgrade: SessionUpgrade) = with(upgrade) {
        engineInteractor.upgrade(
            topic, permissions.toEngineSessionPermissions(),
            { (topic, permissions) -> sessionUpgrade.onSuccess(UpgradedSession(topic, permissions.toClientPerms())) },
            { error -> sessionUpgrade.onError(error) })
    }

    fun update(update: Update, sessionUpdate: SessionUpdate) = with(update) {
        engineInteractor.update(
            sessionTopic, sessionState.toEngineSessionState(),
            { (topic, accounts) -> sessionUpdate.onSuccess(UpdatedSession(topic, accounts)) },
            { error -> sessionUpdate.onError(error) })
    }

    fun ping(ping: Ping, sessionPing: SessionPing) {
        engineInteractor.ping(ping.topic,
            { topic -> sessionPing.onSuccess(topic) },
            { error -> sessionPing.onError(error) })
    }

    fun notify(notify: Notify, notificationListener: NotificationListener) = with(notify) {
        engineInteractor.notify(topic, notification.toEngineNotification(),
            { topic -> notificationListener.onSuccess(topic) },
            { error -> notificationListener.onError(error) })
    }

    fun disconnect(
        disconnectParams: Disconnect,
        listener: SessionDelete
    ) = with(disconnectParams) {
        engineInteractor.disconnect(
            sessionTopic, reason,
            { (topic, reason) -> listener.onSuccess(DeletedSession(topic, reason)) },
            { error -> listener.onError(error) })
    }

    fun getListOfSettledSessions(): List<SettledSession> =
        engineInteractor.getListOfSettledSessions().map(EngineDO.SettledSession::toClientSettledSession)

    fun getListOfPendingSession(): List<SessionProposal> =
        engineInteractor.getListOfPendingSessions().map(EngineDO.SessionProposal::toClientSessionProposal)

    fun shutdown() {
        scope.cancel()
    }

    interface Delegate {
        fun onSessionProposal(sessionProposal: SessionProposal)
        fun onSessionRequest(sessionRequest: SessionRequest)
        fun onSessionDelete(deletedSession: DeletedSession)
        fun onSessionNotification(sessionNotification: SessionNotification)
    }

    sealed interface Listeners {
        fun onError(error: Throwable)
    }

    sealed class Model

    sealed class Params {
        companion object {
            internal const val WALLET_CONNECT_URL = "relay.walletconnect.com"
        }
    }
}