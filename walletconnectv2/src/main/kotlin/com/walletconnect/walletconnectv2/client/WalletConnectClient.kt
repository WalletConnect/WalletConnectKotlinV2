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
        // TODO: re-init scope
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
                    //Responses
                    is EngineDO.SettledPairingResponse -> delegate.onPairingSettledResponse(event.toClientSettledPairingResponse())
                    is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                    is EngineDO.SessionUpgradeResponse -> delegate.onSessionUpgradeResponse(event.toClientUpgradedSessionResponse())
                    is EngineDO.SessionUpdateResponse -> delegate.onSessionUpdateResponse(event.toClientUpdateSessionResponse())
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
                    //Responses
                    is EngineDO.SessionPayloadResponse -> delegate.onSessionPayloadResponse(event.toClientSessionPayloadResponse())
                }
            }
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun connect(connect: WalletConnect.Params.Connect, onFailure: (WalletConnect.Model.Error) -> Unit = {}): WalletConnect.Model.ProposedSequence {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        val proposedSequence = engineInteractor.proposeSequence(connect.permissions.toEngineSessionPermissions(), connect.pairingTopic) { error ->
            onFailure(WalletConnect.Model.Error(error))
        }

        return proposedSequence.toClientProposedSequence()
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun pair(pair: WalletConnect.Params.Pair, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.pair(pair.uri) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun approve(approve: WalletConnect.Params.Approve, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.approve(approve.proposal.toEngineSessionProposal(approve.accounts)) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun reject(reject: WalletConnect.Params.Reject, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.reject(reject.rejectionReason, reject.proposalTopic) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun respond(response: WalletConnect.Params.Response, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.respondSessionPayload(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO()) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun request(request: WalletConnect.Params.Request, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.sessionRequest(request.toEngineDORequest()) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun upgrade(upgrade: WalletConnect.Params.Upgrade, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.upgrade(upgrade.topic, upgrade.permissions.toEngineSessionPermissions()) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun update(update: WalletConnect.Params.Update, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.update(update.sessionTopic, update.sessionState.toEngineSessionState()) { error ->
            onError(WalletConnect.Model.Error(error))
        }
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
    fun notify(notify: WalletConnect.Params.Notify, onError: (Throwable) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.notify(notify.topic, notify.notification.toEngineNotification()) { error -> onError(error) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun disconnect(disconnect: WalletConnect.Params.Disconnect) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason, disconnect.reasonCode)
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledSessions(): List<WalletConnect.Model.SettledSession> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledSessions().map(EngineDO.SettledSession::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledPairings(): List<WalletConnect.Model.SettledPairing> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledPairings().map(EngineDO.SettledPairing::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    fun getPendingRequests(topic: String): List<WalletConnect.Model.PendingRequest> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getPendingRequests(TopicVO(topic)).mapToPendingRequests()
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

        //Responses
        fun onPairingSettledResponse(pairingResponse: WalletConnect.Model.SettledPairingResponse)
        fun onSessionSettleResponse(settleSessionResponse: WalletConnect.Model.SettledSessionResponse)
        fun onSessionUpgradeResponse(sessionUpgradeResponse: WalletConnect.Model.SessionUpgradeResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: WalletConnect.Model.SessionUpdateResponse)
    }

    interface DappDelegate {
        fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing)
        fun onPairingUpdated(pairing: WalletConnect.Model.PairingUpdate)
        fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession)
        fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)

        //Responses
        fun onSessionPayloadResponse(response: WalletConnect.Model.SessionPayloadResponse)
    }
}