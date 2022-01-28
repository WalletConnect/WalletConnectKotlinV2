package com.walletconnect.walletconnectv2.client

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
import java.lang.IllegalStateException

object WalletConnectClient {

    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var engineInteractor: EngineInteractor

    fun initialize(initial: WalletConnect.Params.Init) = with(initial) {
        // TODO: add logic to check hostName for ws/wss scheme with and without ://
        wcKoinApp.run {
            androidContext(application)

            modules(
                commonModule(),
                cryptoManager(),
                networkModule(serverUrl),
                relayerModule(),
                storageModule(),
                engineModule(metadata, isController)
            )
        }

        engineInteractor = wcKoinApp.koin.get()
    }

    @Throws(IllegalStateException::class)
    fun setWalletDelegate(delegate: WalletDelegate) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

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

    @Throws(IllegalStateException::class)
    fun setDappDelegate(delegate: DappDelegate) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is EngineDO.SettledPairing -> delegate.onPairingSettled(event.toClientSettledPairing())
                    is EngineDO.SessionRejected -> delegate.onSessionRejected(event.toClientSessionRejected())
                    is EngineDO.SessionApproved -> delegate.onSessionApproved(event.toClientSessionApproved())
                }
            }
        }
    }

    fun connect(permissions: WalletConnect.Model.SessionPermissions, pairingTopic: String? = null): String? =
        engineInteractor.proposeSequence(permissions.toEngineSessionPermissions(), pairingTopic)

    @Throws(IllegalStateException::class)
    fun pair(pair: WalletConnect.Params.Pair, pairing: WalletConnect.Listeners.Pairing) {
        engineInteractor.pair(pair.uri,
            { topic -> pairing.onSuccess(WalletConnect.Model.SettledPairing(topic)) },
            { error -> pairing.onError(error) })
    }

    @Throws(IllegalStateException::class)
    fun approve(approve: WalletConnect.Params.Approve, sessionApprove: WalletConnect.Listeners.SessionApprove? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(approve) {
            engineInteractor.approve(
                proposal.toEngineSessionProposal(accounts),
                { settledSession -> sessionApprove?.onSuccess(settledSession.toClientSettledSession()) },
                { error -> sessionApprove?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun reject(reject: WalletConnect.Params.Reject, sessionReject: WalletConnect.Listeners.SessionReject? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(reject) {
            engineInteractor.reject(
                rejectionReason, proposalTopic,
                { (topic, reason) -> sessionReject?.onSuccess(WalletConnect.Model.RejectedSession(topic, reason)) },
                { error -> sessionReject?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun respond(response: WalletConnect.Params.Response, sessionPayload: WalletConnect.Listeners.SessionPayload? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(response) {
            engineInteractor.respondSessionPayload(sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO())
            { error -> sessionPayload?.onError(error) }
        }
    }

    @Throws(IllegalStateException::class)
    fun upgrade(upgrade: WalletConnect.Params.Upgrade, sessionUpgrade: WalletConnect.Listeners.SessionUpgrade? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(upgrade) {
            engineInteractor.upgrade(
                topic, permissions.toEngineSessionPermissions(),
                { (topic, permissions) -> sessionUpgrade?.onSuccess(WalletConnect.Model.UpgradedSession(topic, permissions.toClientPerms())) },
                { error -> sessionUpgrade?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun update(update: WalletConnect.Params.Update, sessionUpdate: WalletConnect.Listeners.SessionUpdate? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(update) {
            engineInteractor.update(
                sessionTopic, sessionState.toEngineSessionState(),
                { (topic, accounts) -> sessionUpdate?.onSuccess(WalletConnect.Model.UpdatedSession(topic, accounts)) },
                { error -> sessionUpdate?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun ping(ping: WalletConnect.Params.Ping, sessionPing: WalletConnect.Listeners.SessionPing? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.ping(ping.topic,
            { topic -> sessionPing?.onSuccess(topic) },
            { error -> sessionPing?.onError(error) })
    }

    @Throws(IllegalStateException::class)
    fun notify(notify: WalletConnect.Params.Notify, notificationListener: WalletConnect.Listeners.Notification? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(notify) {
            engineInteractor.notify(topic, notification.toEngineNotification(),
                { topic -> notificationListener?.onSuccess(topic) },
                { error -> notificationListener?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun disconnect(
        disconnectParams: WalletConnect.Params.Disconnect,
        listener: WalletConnect.Listeners.SessionDelete? = null
    ) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(disconnectParams) {
            engineInteractor.disconnect(
                sessionTopic, reason,
                { (topic, reason) -> listener?.onSuccess(WalletConnect.Model.DeletedSession(topic, reason)) },
                { error -> listener?.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledSessions(): List<WalletConnect.Model.SettledSession> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledSessions().map(EngineDO.SettledSession::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    fun getListOfPendingSession(): List<WalletConnect.Model.SessionProposal> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfPendingSessions().map(EngineDO.SessionProposal::toClientSessionProposal)
    }

    fun shutdown() {
        scope.cancel()
        wcKoinApp.close()
    }

    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal)
        fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)
        fun onSessionNotification(sessionNotification: WalletConnect.Model.SessionNotification)
    }

    interface DappDelegate {
        fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing)
        fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession)
    }
}