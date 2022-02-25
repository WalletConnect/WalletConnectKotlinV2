package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.scope.scope
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

    fun initialize(initial: WalletConnect.Params.Init, onError: (WalletConnectException) -> Unit = {}) = with(initial) {
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
        engineInteractor.handleInitializationErrors { walletConnectException -> onError(walletConnectException) }
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
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
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
                    is EngineDO.PairingUpdate -> delegate.onPairingUpdated(event.toClientSettledPairing())
                    is EngineDO.SessionRejected -> delegate.onSessionRejected(event.toClientSessionRejected())
                    is EngineDO.SessionApproved -> delegate.onSessionApproved(event.toClientSessionApproved())
                    is EngineDO.SessionUpdate -> delegate.onSessionUpdate(event.toClientSessionsUpdate())
                    is EngineDO.SessionUpgrade -> delegate.onSessionUpgrade(event.toClientSessionsUpgrade())
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                }
            }
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun connect(connect: WalletConnect.Params.Connect, connecting: WalletConnect.Listeners.Connect? = null): String? {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.proposeSequence(connect.permissions.toEngineSessionPermissions(), connect.pairingTopic)
        { error -> connecting?.onError(WalletConnect.Model.WCException(error)) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun pair(pair: WalletConnect.Params.Pair, pairing: WalletConnect.Listeners.Pairing? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.pair(pair.uri,
            { topic -> pairing?.onSuccess(WalletConnect.Model.SettledPairing(topic)) },
            { error -> pairing?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class)
    fun approve(approve: WalletConnect.Params.Approve, sessionApprove: WalletConnect.Listeners.SessionApprove? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.approve(
            approve.proposal.toEngineSessionProposal(approve.accounts),
            { settledSession -> sessionApprove?.onSuccess(settledSession.toClientSettledSession()) },
            { error -> sessionApprove?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class)
    fun reject(reject: WalletConnect.Params.Reject, sessionReject: WalletConnect.Listeners.SessionReject? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.reject(
            reject.rejectionReason, reject.proposalTopic,
            { (topic, reason) -> sessionReject?.onSuccess(WalletConnect.Model.RejectedSession(topic, reason)) },
            { error -> sessionReject?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun respond(response: WalletConnect.Params.Response, sessionPayload: WalletConnect.Listeners.SessionPayload? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.respondSessionPayload(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO())
        { error -> sessionPayload?.onError(WalletConnect.Model.WCException(error)) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun request(request: WalletConnect.Params.Request, sessionRequest: WalletConnect.Listeners.SessionRequest? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.sessionRequest(request.toEngineDORequest(),
            { jsonRpcResult -> sessionRequest?.onSuccess(jsonRpcResult.toClientJsonRpcResult()) },
            { error -> sessionRequest?.onError(WalletConnect.Model.JsonRpcResponse.Error(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun upgrade(upgrade: WalletConnect.Params.Upgrade, sessionUpgrade: WalletConnect.Listeners.SessionUpgrade? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.upgrade(
            upgrade.topic, upgrade.permissions.toEngineSessionPermissions(),
            { (topic, permissions) -> sessionUpgrade?.onSuccess(WalletConnect.Model.UpgradedSession(topic, permissions.toClientPerms())) },
            { error -> sessionUpgrade?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun update(update: WalletConnect.Params.Update, sessionUpdate: WalletConnect.Listeners.SessionUpdate? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.update(
            update.sessionTopic, update.sessionState.toEngineSessionState(),
            { (topic, accounts) -> sessionUpdate?.onSuccess(WalletConnect.Model.UpdatedSession(topic, accounts)) },
            { error -> sessionUpdate?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun ping(ping: WalletConnect.Params.Ping, sessionPing: WalletConnect.Listeners.SessionPing? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.ping(ping.topic,
            { topic -> sessionPing?.onSuccess(WalletConnect.Model.Ping.Success(topic)) },
            { error -> sessionPing?.onError(WalletConnect.Model.Ping.Error(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun notify(notify: WalletConnect.Params.Notify, notificationListener: WalletConnect.Listeners.Notification? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.notify(notify.topic, notify.notification.toEngineNotification(),
            { topic -> notificationListener?.onSuccess(topic) },
            { error -> notificationListener?.onError(WalletConnect.Model.WCException(error)) })
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun disconnect(disconnect: WalletConnect.Params.Disconnect, listener: WalletConnect.Listeners.SessionDelete? = null) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason, disconnect.reasonCode,
            { (topic, reason) -> listener?.onSuccess(WalletConnect.Model.DeletedSession.Success(topic, reason)) },
            { error -> listener?.onError(WalletConnect.Model.DeletedSession.Error(error)) })
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

    @Throws(IllegalStateException::class)
    fun getJsonRpcHistory(topic: String): WalletConnect.Model.JsonRpcHistory {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        val (listOfRequests, listOfResponses) = engineInteractor.getListOfJsonRpcHistory(TopicVO(topic))

        return WalletConnect.Model.JsonRpcHistory(
            topic = topic,
            listOfRequests = listOfRequests.mapToHistory(),
            listOfResponses = listOfResponses.mapToHistory()
        )
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
        fun onPairingUpdated(pairing: WalletConnect.Model.SettledPairing)
        fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession)
        fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)
    }
}