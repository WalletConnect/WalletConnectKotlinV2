package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.common.errors.WalletConnectExceptions
import com.walletconnect.walletconnectv2.common.scope.scope
import com.walletconnect.walletconnectv2.di.*
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

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
                    is EngineDO.SessionRequest -> delegate.onSessionRequest(event.toClientSessionRequest())  //eth_sign
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

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun connect(connect: WalletConnect.Params.Connect): String? {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return safeCallWithReturn {
            engineInteractor.proposeSequence(connect.permissions.toEngineSessionPermissions(), connect.pairingTopic)
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun pair(pair: WalletConnect.Params.Pair, pairing: WalletConnect.Listeners.Pairing) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.pair(pair.uri,
                { topic -> pairing.onSuccess(WalletConnect.Model.SettledPairing(topic)) },
                { error -> pairing.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun approve(approve: WalletConnect.Params.Approve, sessionApprove: WalletConnect.Listeners.SessionApprove) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(approve) {
            engineInteractor.approve(
                proposal.toEngineSessionProposal(accounts),
                { settledSession -> sessionApprove.onSuccess(settledSession.toClientSettledSession()) },
                { error -> sessionApprove.onError(error) })
        }
    }

    @Throws(IllegalStateException::class)
    fun reject(reject: WalletConnect.Params.Reject, sessionReject: WalletConnect.Listeners.SessionReject) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        with(reject) {
            engineInteractor.reject(
                rejectionReason, proposalTopic,
                { (topic, reason) -> sessionReject.onSuccess(WalletConnect.Model.RejectedSession(topic, reason)) },
                { error -> sessionReject.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun respond(response: WalletConnect.Params.Response, sessionPayload: WalletConnect.Listeners.SessionPayload) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.respondSessionPayload(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO())
            { error -> sessionPayload.onError(error) }
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun request(request: WalletConnect.Params.Request, sessionRequest: WalletConnect.Listeners.SessionRequest) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.sessionRequest(request.toEngineDORequest(),
                { jsonRpcResult -> sessionRequest.onSuccess(jsonRpcResult.toClientJsonRpcResult()) },
                { error -> sessionRequest.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun upgrade(upgrade: WalletConnect.Params.Upgrade, sessionUpgrade: WalletConnect.Listeners.SessionUpgrade) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.upgrade(
                upgrade.topic, upgrade.permissions.toEngineSessionPermissions(),
                { (topic, permissions) -> sessionUpgrade.onSuccess(WalletConnect.Model.UpgradedSession(topic, permissions.toClientPerms())) },
                { error -> sessionUpgrade.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun update(update: WalletConnect.Params.Update, sessionUpdate: WalletConnect.Listeners.SessionUpdate) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.update(
                update.sessionTopic, update.sessionState.toEngineSessionState(),
                { (topic, accounts) -> sessionUpdate.onSuccess(WalletConnect.Model.UpdatedSession(topic, accounts)) },
                { error -> sessionUpdate.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun ping(ping: WalletConnect.Params.Ping, sessionPing: WalletConnect.Listeners.SessionPing) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.ping(ping.topic, { topic -> sessionPing.onSuccess(topic) }, { error -> sessionPing.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun notify(notify: WalletConnect.Params.Notify, notificationListener: WalletConnect.Listeners.Notification) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.notify(notify.topic, notify.notification.toEngineNotification(),
                { topic -> notificationListener.onSuccess(topic) },
                { error -> notificationListener.onError(error) })
        }
    }

    @Throws(IllegalStateException::class, WalletConnect.WalletConnectError::class)
    fun disconnect(disconnect: WalletConnect.Params.Disconnect, listener: WalletConnect.Listeners.SessionDelete) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        safeCall {
            engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason,
                { (topic, reason) -> listener.onSuccess(WalletConnect.Model.DeletedSession(topic, reason)) },
                { error -> listener.onError(error) })
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

    @Throws(IllegalStateException::class)
    fun getListOfSettledPairings(): List<WalletConnect.Model.SettledPairing> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledPairings().map(EngineDO.SettledPairing::toClientSettledPairing)
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

    private fun safeCall(method: () -> Unit) {
        try {
            method()
        } catch (e: WalletConnectExceptions) {
            throw WalletConnect.WalletConnectError(e.message)
        }
    }

    private fun safeCallWithReturn(method: () -> String?): String? {
        try {
            return method()
        } catch (e: WalletConnectExceptions) {
            throw WalletConnect.WalletConnectError(e.message)
        }
    }
}