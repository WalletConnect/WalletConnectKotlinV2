package com.walletconnect.walletconnectv2.client

import android.util.Log
import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.client.mapper.toClientSessionProposal
import com.walletconnect.walletconnectv2.client.mapper.toClientSettledSession
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.di.*
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

object WalletConnectClient {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private val engineInteractor: EngineInteractor by wcKoinApp.koin.inject()

    fun initialize(initial: WalletConnect.Params.Init) = with(initial) {
        // TODO: add logic to check hostName for ws/wss scheme with and without ://
        wcKoinApp.run {
            androidContext(application)

            modules(
                commonModule(),
                cryptoManager(),
                networkModule(useTls, hostName, projectId),
                relayerModule(),
                storageModule(),
                engineModule(metadata, isController)
            )
        }
    }

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

    fun pair(pair: WalletConnect.Params.Pair, pairing: WalletConnect.Listeners.Pairing) {
        engineInteractor.pair(pair.uri,
            { topic -> pairing.onSuccess(WalletConnect.Model.SettledPairing(topic)) },
            { error -> pairing.onError(error) })
    }

    fun approve(approve: WalletConnect.Params.Approve, sessionApprove: WalletConnect.Listeners.SessionApprove) = with(approve) {
        engineInteractor.approve(
            proposal.toEngineSessionProposal(accounts),
            { settledSession -> sessionApprove.onSuccess(settledSession.toClientSettledSession()) },
            { error -> sessionApprove.onError(error) })
    }

    fun reject(reject: WalletConnect.Params.Reject, sessionReject: WalletConnect.Listeners.SessionReject) = with(reject) {
        engineInteractor.reject(
            rejectionReason, proposalTopic,
            { (topic, reason) -> sessionReject.onSuccess(WalletConnect.Model.RejectedSession(topic, reason)) },
            { error -> sessionReject.onError(error) })
    }

    fun respond(response: WalletConnect.Params.Response, sessionPayload: WalletConnect.Listeners.SessionPayload) = with(response) {
        engineInteractor.respondSessionPayload(sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO())
        { error -> sessionPayload.onError(error) }
    }

    fun upgrade(upgrade: WalletConnect.Params.Upgrade, sessionUpgrade: WalletConnect.Listeners.SessionUpgrade) = with(upgrade) {
        engineInteractor.upgrade(
            topic, permissions.toEngineSessionPermissions(),
            { (topic, permissions) -> sessionUpgrade.onSuccess(WalletConnect.Model.UpgradedSession(topic, permissions.toClientPerms())) },
            { error -> sessionUpgrade.onError(error) })
    }

    fun update(update: WalletConnect.Params.Update, sessionUpdate: WalletConnect.Listeners.SessionUpdate) = with(update) {
        engineInteractor.update(
            sessionTopic, sessionState.toEngineSessionState(),
            { (topic, accounts) -> sessionUpdate.onSuccess(WalletConnect.Model.UpdatedSession(topic, accounts)) },
            { error -> sessionUpdate.onError(error) })
    }

    fun ping(ping: WalletConnect.Params.Ping, sessionPing: WalletConnect.Listeners.SessionPing) {
        engineInteractor.ping(ping.topic,
            { topic -> sessionPing.onSuccess(topic) },
            { error -> sessionPing.onError(error) })
    }

    fun notify(notify: WalletConnect.Params.Notify, notificationListener: WalletConnect.Listeners.NotificationListener) = with(notify) {
        engineInteractor.notify(topic, notification.toEngineNotification(),
            { topic -> notificationListener.onSuccess(topic) },
            { error -> notificationListener.onError(error) })
    }

    fun disconnect(
        disconnectParams: WalletConnect.Params.Disconnect,
        listener: WalletConnect.Listeners.SessionDelete
    ) = with(disconnectParams) {
        engineInteractor.disconnect(
            sessionTopic, reason,
            { (topic, reason) -> listener.onSuccess(WalletConnect.Model.DeletedSession(topic, reason)) },
            { error -> listener.onError(error) })
    }

    fun getListOfSettledSessions(): List<WalletConnect.Model.SettledSession> =
        engineInteractor.getListOfSettledSessions().map(EngineDO.SettledSession::toClientSettledSession)

    fun getListOfPendingSession(): List<WalletConnect.Model.SessionProposal> =
        engineInteractor.getListOfPendingSessions().map(EngineDO.SessionProposal::toClientSessionProposal)

    fun shutdown() {
        scope.cancel()
    }

    interface Delegate {
        fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal)
        fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)
        fun onSessionNotification(sessionNotification: WalletConnect.Model.SessionNotification)
    }
}