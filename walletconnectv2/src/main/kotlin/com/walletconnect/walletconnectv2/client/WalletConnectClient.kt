package com.walletconnect.walletconnectv2.client

import com.walletconnect.walletconnectv2.client.mapper.*
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.scope.scope
import com.walletconnect.walletconnectv2.di.*
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.network.Relay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

object WalletConnectClient {

    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var engineInteractor: EngineInteractor
    private lateinit var relay: Relay

    fun initialize(initial: WalletConnect.Params.Init, onError: (WalletConnect.Model.Error) -> Unit) {
        with(initial) {
            // TODO: re-init scope
            // TODO: add logic to check hostName for ws/wss scheme with and without ://
            wcKoinApp.run {
                androidContext(application)
                modules(
                    commonModule(),
                    cryptoManager(),
                    networkModule(serverUrl, relay, connectionType.toRelayConnectionType()),
                    relayerModule(),
                    storageModule(),
                    engineModule(metadata)
                )
            }
        }
        engineInteractor = wcKoinApp.koin.get()
        engineInteractor.handleInitializationErrors { error -> onError(WalletConnect.Model.Error(error)) }
        relay = wcKoinApp.koin.get()
    }

    @Throws(IllegalStateException::class)
    fun setWalletDelegate(delegate: WalletDelegate) {
        checkEngineInitialization()
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
                    //Utils
                    is EngineDO.ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    fun setDappDelegate(delegate: DappDelegate) {
    checkEngineInitialization()
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
                    //Utils
                    is EngineDO.ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                }
            }
        }
    }

    object WebSocket {
        fun open(onError: (String) -> Unit) {
            relay.connect { errorMessage -> onError(errorMessage) }
        }

        fun close(onError: (String) -> Unit) {
            relay.disconnect { errorMessage -> onError(errorMessage) }
        }
    }

    @Throws(IllegalStateException::class)
    fun connect(
        connect: WalletConnect.Params.Connect, onProposedSequence: (WalletConnect.Model.ProposedSequence) -> Unit,
        onError: (WalletConnect.Model.Error) -> Unit
    ) {
        checkEngineInitialization()
        try {
            engineInteractor.proposeSequence(
                connect.chains, connect.methods, connect.events, connect.pairingTopic,
                onProposedSequence = { proposedSequence -> onProposedSequence(proposedSequence.toClientProposedSequence()) },
                onFailure = { error -> onError(WalletConnect.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun pair(pair: WalletConnect.Params.Pair, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.pair(pair.uri)
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun approveSession(approve: WalletConnect.Params.Approve, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.approve(approve.proposerPublicKey, approve.accounts, approve.methods, approve.events) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun rejectSession(reject: WalletConnect.Params.Reject, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.reject(reject.proposerPublicKey, reject.reason, reject.code) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun respond(response: WalletConnect.Params.Response, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO()) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun request(request: WalletConnect.Params.Request, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.sessionRequest(request.toEngineDORequest()) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun updateAccounts(updateAccounts: WalletConnect.Params.UpdateAccounts, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.updateSessionAccounts(updateAccounts.sessionTopic, updateAccounts.accounts) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun updateMethods(updateMethods: WalletConnect.Params.UpdateMethods, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.updateSessionMethods(updateMethods.sessionTopic, updateMethods.methods) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun updateEvents(updateEvents: WalletConnect.Params.UpdateEvents, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.updateSessionEvents(updateEvents.sessionTopic, updateEvents.events) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun updateExpiry(updateExpiry: WalletConnect.Params.UpdateExpiry, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.updateSessionExpiry(updateExpiry.topic, updateExpiry.newExpiration) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun emit(emit: WalletConnect.Params.Emit, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.emit(emit.topic, emit.event.toEngineEvent(emit.chainId)) { error ->
                onError(WalletConnect.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun ping(ping: WalletConnect.Params.Ping, sessionPing: WalletConnect.Listeners.SessionPing? = null) {
        checkEngineInitialization()
        try {
            engineInteractor.ping(
                ping.topic,
                { topic -> sessionPing?.onSuccess(WalletConnect.Model.Ping.Success(topic)) },
                { error -> sessionPing?.onError(WalletConnect.Model.Ping.Error(error)) }
            )
        } catch (error: Exception) {
            sessionPing?.onError(WalletConnect.Model.Ping.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun disconnect(disconnect: WalletConnect.Params.Disconnect, onError: (WalletConnect.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason, disconnect.reasonCode)
        } catch (error: Exception) {
            onError(WalletConnect.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledSessions(): List<WalletConnect.Model.Session> {
        checkEngineInitialization()
        return engineInteractor.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    fun getListOfSettledPairings(): List<WalletConnect.Model.Pairing> {
        checkEngineInitialization()
        return engineInteractor.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    fun getPendingRequests(topic: String): List<WalletConnect.Model.PendingRequest> {
        checkEngineInitialization()
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

        //Utils
        fun onConnectionStateChange(state: WalletConnect.Model.ConnectionState)
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

        // Utils
        fun onConnectionStateChange(state: WalletConnect.Model.ConnectionState)
    }


    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }
    }
}