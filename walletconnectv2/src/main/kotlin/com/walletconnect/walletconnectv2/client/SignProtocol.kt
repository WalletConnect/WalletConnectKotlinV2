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

internal class SignProtocol : SignInterface, SignInterface.Websocket {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var engineInteractor: EngineInteractor
    override val relay: Relay by lazy { wcKoinApp.koin.get() }

    companion object  {
        val instance = SignProtocol()
    }

    override fun initialize(initial: Sign.Params.Init, onError: (Sign.Model.Error) -> Unit) {
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
        engineInteractor.handleInitializationErrors { error -> onError(Sign.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        checkEngineInitialization()
        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is EngineDO.SessionProposal -> delegate.onSessionProposal(event.toClientSessionProposal())
                    is EngineDO.SessionRequest -> delegate.onSessionRequest(event.toClientSessionRequest())
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                    //Responses
                    is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                    is EngineDO.SessionUpdateNamespacesResponse -> delegate.onSessionUpdateResponse(event.toClientUpdateSessionNamespacesResponse())
                    //Utils
                    is EngineDO.ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun setDappDelegate(delegate: SignInterface.DappDelegate) {
        checkEngineInitialization()
        scope.launch {
            engineInteractor.sequenceEvent.collect { event ->
                when (event) {
                    is EngineDO.SessionRejected -> delegate.onSessionRejected(event.toClientSessionRejected())
                    is EngineDO.SessionApproved -> delegate.onSessionApproved(event.toClientSessionApproved())
                    is EngineDO.SessionUpdateNamespaces -> delegate.onSessionUpdate(event.toClientSessionsNamespaces())
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                    is EngineDO.SessionEvent -> delegate.onSessionEvent(event.toClientSessionEvent())
                    is EngineDO.SessionExtend -> delegate.onSessionExtend(event.toClientSettledSession())
                    //Responses
                    is EngineDO.SessionPayloadResponse -> delegate.onSessionRequestResponse(event.toClientSessionPayloadResponse())
                    //Utils
                    is EngineDO.ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun connect(
        connect: Sign.Params.Connect, onProposedSequence: (Sign.Model.ProposedSequence) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        try {
            engineInteractor.proposeSequence(
                connect.namespaces.toMapOfEngineNamespacesProposal(),
                connect.relays?.toListEngineOfRelayProtocolOptions(),
                connect.pairingTopic,
                { proposedSequence -> onProposedSequence(proposedSequence.toClientProposedSequence()) },
                { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(pair: Sign.Params.Pair, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.pair(pair.uri)
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun approveSession(approve: Sign.Params.Approve, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.approve(approve.proposerPublicKey, approve.namespaces.toMapOfEngineNamespacesSession()) { error ->
                onError(Sign.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun rejectSession(reject: Sign.Params.Reject, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.reject(reject.proposerPublicKey, reject.reason, reject.code) { error ->
                onError(Sign.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun request(request: Sign.Params.Request, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.sessionRequest(request.toEngineDORequest()) { error ->
                onError(Sign.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(response: Sign.Params.Response, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO()) { error ->
                onError(Sign.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun update(update: Sign.Params.Update, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.updateSession(update.sessionTopic, update.namespaces.toMapOfEngineNamespacesSession())
            { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun extend(extend: Sign.Params.Extend, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.extend(extend.topic) { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun emit(emit: Sign.Params.Emit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.emit(emit.topic, emit.event.toEngineEvent(emit.chainId)) { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing?) {
        checkEngineInitialization()
        try {
            engineInteractor.ping(
                ping.topic,
                { topic -> sessionPing?.onSuccess(Sign.Model.Ping.Success(topic)) },
                { error -> sessionPing?.onError(Sign.Model.Ping.Error(error)) }
            )
        } catch (error: Exception) {
            sessionPing?.onError(Sign.Model.Ping.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun disconnect(disconnect: Sign.Params.Disconnect, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            engineInteractor.disconnect(disconnect.sessionTopic, disconnect.reason, disconnect.reasonCode)
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return engineInteractor.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
        checkEngineInitialization()
        return engineInteractor.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        checkEngineInitialization()
        return engineInteractor.getPendingRequests(TopicVO(topic)).mapToPendingRequests()
    }

    // TODO: Uncomment once reinit scope logic is added
//    fun shutdown() {
//        scope.cancel()
//        wcKoinApp.close()
//    }


    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::engineInteractor.isInitialized) {
            "WalletConnectClient needs to be initialized first using the initialize function"
        }
    }
}