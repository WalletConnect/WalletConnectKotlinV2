@file:JvmSynthetic

package com.walletconnect.sign.client

import com.walletconnect.sign.client.mapper.*
import com.walletconnect.sign.core.exceptions.client.WalletConnectException
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.scope.scope
import com.walletconnect.sign.crypto.data.repository.JwtRepository
import com.walletconnect.sign.di.*
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.network.Relay
import com.walletconnect.sign.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

internal class SignProtocol : SignInterface, SignInterface.Websocket {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var signEngine: SignEngine
    override val relay: Relay by lazy { wcKoinApp.koin.get() }
    private val mutex = Mutex()

    companion object {
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
                    networkModule(nonceServerUrl),
                    relayerModule(),
                    storageModule(),
                    engineModule(metadata)
                )
            }
        }

        scope.launch {
            val jwtRepository = wcKoinApp.koin.get<JwtRepository>()

            withContext(coroutineContext) {
                mutex.withLock {
                    val nonce = jwtRepository.getNonceFromDID()

                    if (nonce != null) {
                        val jwt = jwtRepository.signJWT(nonce)
                        wcKoinApp.modules(scarletModule(initial.relayServerUrl, jwt, initial.connectionType.toRelayConnectionType(), initial.relay))
                        signEngine = wcKoinApp.koin.get()
                        signEngine.handleInitializationErrors { error -> onError(Sign.Model.Error(error)) }
                        Logger.log("Engine Initialized")
                    } else {
                        onError(Sign.Model.Error(WalletConnectException.GenericException("Unable to generate Nonce. Please check connection")))
                    }
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        scope.launch {
            mutex.withLock {
                checkEngineInitialization()
                signEngine.engineEvent.collect { event ->
                    when (event) {
                        is EngineDO.SessionProposal -> delegate.onSessionProposal(event.toClientSessionProposal())
                        is EngineDO.SessionRequest -> delegate.onSessionRequest(event.toClientSessionRequest())
                        is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                        //Responses
                        is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                        is EngineDO.SessionUpdateNamespacesResponse -> delegate.onSessionUpdateResponse(event.toClientUpdateSessionNamespacesResponse())
                        //Utils
                        is EngineDO.ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                        is EngineDO.InternalError -> delegate.onError(event.toClientError())
                    }
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun setDappDelegate(delegate: SignInterface.DappDelegate) {
        scope.launch {
            mutex.withLock {
                checkEngineInitialization()
                signEngine.engineEvent.collect { event ->
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
                        is EngineDO.InternalError -> delegate.onError(event.toClientError())
                    }
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
            signEngine.proposeSequence(
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
            signEngine.pair(pair.uri)
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun approveSession(approve: Sign.Params.Approve, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.approve(approve.proposerPublicKey, approve.namespaces.toMapOfEngineNamespacesSession()) { error ->
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
            signEngine.reject(reject.proposerPublicKey, reject.reason, reject.code) { error ->
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
            signEngine.sessionRequest(request.toEngineDORequest()) { error ->
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
            signEngine.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponseVO()) { error ->
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
            signEngine.updateSession(update.sessionTopic, update.namespaces.toMapOfEngineNamespacesSession())
            { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun extend(extend: Sign.Params.Extend, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.extend(extend.topic) { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun emit(emit: Sign.Params.Emit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.emit(emit.topic, emit.event.toEngineEvent(emit.chainId)) { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing?) {
        checkEngineInitialization()
        try {
            signEngine.ping(
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
            signEngine.disconnect(disconnect.sessionTopic)
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
//        checkEngineInitialization()
        return signEngine.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        checkEngineInitialization()
        return signEngine.getPendingRequests(TopicVO(topic)).mapToPendingRequests()
    }

    // TODO: Uncomment once reinit scope logic is added
//    fun shutdown() {
//        scope.cancel()
//        wcKoinApp.close()
//    }


    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::signEngine.isInitialized) {
            "SignClient needs to be initialized first using the initialize function"
        }
    }
}