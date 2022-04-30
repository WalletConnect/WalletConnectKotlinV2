package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.di.*
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineDO
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
                engineModule(metadata)
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
                    is EngineDO.SessionEvent -> delegate.onSessionEvent(event.toClientSessionEvent())
                    //Responses
                    is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                    is EngineDO.SessionUpdateAccountsResponse -> delegate.onSessionUpdateAccountsResponse(event.toClientUpdateSessionAccountsResponse())
                    is EngineDO.SessionUpdateMethodsResponse -> delegate.onSessionUpdateMethodsResponse(event.toClientUpdateSessionMethodsResponse())
                    is EngineDO.SessionUpdateEventsResponse -> delegate.onSessionUpdateEventsResponse(event.toClientUpdateSessionEventsResponse())
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
                    is EngineDO.SessionRejected -> delegate.onSessionRejected(event.toClientSessionRejected())
                    is EngineDO.SessionApproved -> delegate.onSessionApproved(event.toClientSessionApproved())
                    is EngineDO.SessionUpdateAccounts -> delegate.onSessionUpdateAccounts(event.toClientSessionsUpdateAccounts())
                    is EngineDO.SessionUpdateMethods -> delegate.onSessionUpdateMethods(event.toClientSessionsUpdateMethods())
                    is EngineDO.SessionUpdateEvents -> delegate.onSessionUpdateEvents(event.toClientSessionsUpdateEvents())
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                    is EngineDO.SessionUpdateExpiry -> delegate.onUpdateSessionExpiry(event.toClientSettledSession())
                    //Responses
                    is EngineDO.SessionPayloadResponse -> delegate.onSessionRequestResponse(event.toClientSessionPayloadResponse())
                }
            }
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun connect(
        connect: WalletConnect.Params.Connect,
        onProposedSequence: (WalletConnect.Model.ProposedSequence) -> Unit,
        onFailure: (WalletConnect.Model.Error) -> Unit = {},
    ) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.proposeSequence(
            connect.chains,
            connect.methods,
            connect.events,
            connect.pairingTopic,
            onProposedSequence = { proposedSequence -> onProposedSequence(proposedSequence.toClientProposedSequence()) },
            onFailure = { error -> onFailure(WalletConnect.Model.Error(error)) }
        )
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun pair(pair: WalletConnect.Params.Pair) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.pair(pair.uri)
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun approveSession(approve: WalletConnect.Params.Approve, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.approve(approve.proposerPublicKey, approve.accounts, approve.methods, approve.events)
        { error -> onError(WalletConnect.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun rejectSession(reject: WalletConnect.Params.Reject, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.reject(reject.proposerPublicKey, reject.reason, reject.code)
        { error -> onError(WalletConnect.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun respond(response: WalletConnect.Params.Response, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO()) { error ->
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
    fun updateAccounts(updateAccounts: WalletConnect.Params.UpdateAccounts, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.updateSessionAccounts(updateAccounts.sessionTopic, updateAccounts.accounts) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun updateMethods(updateMethods: WalletConnect.Params.UpdateMethods, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.updateSessionMethods(updateMethods.sessionTopic, updateMethods.methods) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun updateEvents(updateEvents: WalletConnect.Params.UpdateEvents, onError: (WalletConnect.Model.Error) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.updateSessionEvents(updateEvents.sessionTopic, updateEvents.events) { error ->
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun updateExpiry(updateExpiry: WalletConnect.Params.UpdateExpiry, onError: (Throwable) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.updateSessionExpiry(updateExpiry.topic, updateExpiry.newExpiration) { error -> onError(error) }
    }

    @Throws(IllegalStateException::class, WalletConnectException::class)
    fun emit(emit: WalletConnect.Params.Emit, onError: (Throwable) -> Unit = {}) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.emit(emit.topic, emit.event.toEngineEvent(emit.chainId)) { error -> onError(error) }
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
    fun disconnect(disconnect: WalletConnect.Params.Disconnect) {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason, disconnect.reasonCode)
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledSessions(): List<WalletConnect.Model.Session> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledPairings(): List<WalletConnect.Model.Pairing> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    fun getPendingRequests(topic: String): List<WalletConnect.Model.PendingRequest> {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }

        return engineInteractor.getPendingRequests(TopicVO(topic)).mapToPendingRequests()
    }

    // TODO: Uncomment once reinit scope logic is added
//    fun shutdown() {
//        scope.cancel()
//        wcKoinApp.close()
//    }

    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal)
        fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)
        fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: WalletConnect.Model.SettledSessionResponse)
        fun onSessionUpdateAccountsResponse(sessionUpdateAccountsResponse: WalletConnect.Model.SessionUpdateAccountsResponse)
        fun onSessionUpdateMethodsResponse(sessionUpdateMethodsResponse: WalletConnect.Model.SessionUpdateMethodsResponse)
        fun onSessionUpdateEventsResponse(sessionUpdateEventsResponse: WalletConnect.Model.SessionUpdateEventsResponse)
    }

    interface DappDelegate {
        fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession)
        fun onSessionUpdateAccounts(updatedSessionAccounts: WalletConnect.Model.UpdatedSessionAccounts)
        fun onSessionUpdateMethods(updatedSessionMethods: WalletConnect.Model.UpdatedSessionMethods)
        fun onSessionUpdateEvents(updatedSessionEvents: WalletConnect.Model.UpdatedSessionEvents)
        fun onUpdateSessionExpiry(session: WalletConnect.Model.Session)
        fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: WalletConnect.Model.SessionRequestResponse)
    }
}