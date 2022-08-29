@file:JvmSynthetic

package com.walletconnect.sign.client

import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.android_core.common.model.SDKError
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.di.cryptoModule
import com.walletconnect.android_core.di.networkModule
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.mapper.*
import com.walletconnect.sign.di.commonModule
import com.walletconnect.sign.di.engineModule
import com.walletconnect.sign.di.jsonRpcModule
import com.walletconnect.sign.di.storageModule
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.engine.model.EngineDO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import java.util.concurrent.Executors

internal class SignProtocol : SignInterface, SignInterface.Websocket {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var signEngine: SignEngine
    internal val relay: RelayConnectionInterface by lazy { wcKoinApp.koin.get() }
    private val mutex = Mutex()
    private val signProtocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    companion object {
        val instance = SignProtocol()
    }

    override fun initialize(initial: Sign.Params.Init, onError: (Sign.Model.Error) -> Unit) {
        signProtocolScope.launch {
            mutex.withLock {
                with(initial) {
                    // TODO: re-init scope
                    // TODO: add logic to check hostName for ws/wss scheme with and without ://
                    wcKoinApp.run {
                        androidContext(application)
                        modules(
                            commonModule(),
                            cryptoModule(),
                            jsonRpcModule(),
                            storageModule(),
                            engineModule(metadata)
                        )
                    }
                }

                val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
                val jwt = jwtRepository.generateJWT(initial.relayServerUrl.strippedUrl())
                val serverUrl = initial.relayServerUrl.addUserAgent(BuildConfig.sdkVersion)
                val connectionType = initial.connectionType.toRelayConnectionType()

                wcKoinApp.modules(networkModule(serverUrl, jwt, connectionType, BuildConfig.sdkVersion,initial.relay))
                signEngine = wcKoinApp.koin.get()
                signEngine.handleInitializationErrors { error -> onError(Sign.Model.Error(error)) }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        awaitLock {
            signEngine.engineEvent.onEach { event ->
                when (event) {
                    is EngineDO.SessionProposal -> delegate.onSessionProposal(event.toClientSessionProposal())
                    is EngineDO.SessionRequest -> delegate.onSessionRequest(event.toClientSessionRequest())
                    is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                    //Responses
                    is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                    is EngineDO.SessionUpdateNamespacesResponse -> delegate.onSessionUpdateResponse(event.toClientUpdateSessionNamespacesResponse())
                    //Utils
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }.launchIn(scope)
        }
    }

    @Throws(IllegalStateException::class)
    override fun setDappDelegate(delegate: SignInterface.DappDelegate) {
        awaitLock {
            signEngine.engineEvent.onEach { event ->
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
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                    is SDKError -> delegate.onError(event.toClientError())
                }
            }.launchIn(scope)
        }
    }

    @Throws(IllegalStateException::class)
    override fun connect(
        connect: Sign.Params.Connect, onProposedSequence: (Sign.Model.ProposedSequence) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        awaitLock {
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
    }

    @Throws(IllegalStateException::class)
    override fun pair(pair: Sign.Params.Pair, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.pair(pair.uri)
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun approveSession(approve: Sign.Params.Approve, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.approve(approve.proposerPublicKey, approve.namespaces.toMapOfEngineNamespacesSession()) { error ->
                    onError(Sign.Model.Error(error))
                }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun rejectSession(reject: Sign.Params.Reject, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.reject(reject.proposerPublicKey, reject.reason, reject.code) { error ->
                    onError(Sign.Model.Error(error))
                }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun request(request: Sign.Params.Request, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.sessionRequest(request.toEngineDORequest()) { error ->
                    onError(Sign.Model.Error(error))
                }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(response: Sign.Params.Response, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponse()) { error ->
                    onError(Sign.Model.Error(error))
                }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun update(update: Sign.Params.Update, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.sessionUpdate(update.sessionTopic, update.namespaces.toMapOfEngineNamespacesSession())
                { error -> onError(Sign.Model.Error(error)) }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun extend(extend: Sign.Params.Extend, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.extend(extend.topic) { error -> onError(Sign.Model.Error(error)) }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun emit(emit: Sign.Params.Emit, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.emit(emit.topic, emit.event.toEngineEvent(emit.chainId)) { error -> onError(Sign.Model.Error(error)) }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing?) {
        awaitLock {
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
    }

    @Throws(IllegalStateException::class)
    override fun disconnect(disconnect: Sign.Params.Disconnect, onError: (Sign.Model.Error) -> Unit) {
        awaitLock {
            try {
                signEngine.disconnect(disconnect.sessionTopic)
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledSessions(): List<Sign.Model.Session> {
        return awaitLock {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
        }
    }

    @Throws(IllegalStateException::class)
    override fun getSettledSessionByTopic(topic: String): Sign.Model.Session? {
        return awaitLock {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
                .find { session -> session.topic == topic }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
        return awaitLock {
            signEngine.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
        }
    }

    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        return awaitLock {
            signEngine.getPendingRequests(Topic(topic)).mapToPendingRequests()
        }
    }

    // TODO: Uncomment once reinit scope logic is added
//    fun shutdown() {
//        scope.cancel()
//        wcKoinApp.close()
//    }

    @Throws(IllegalStateException::class)
    override fun open(onError: (String) -> Unit) {
        awaitLock {
            relay.connect { errorMessage -> onError(errorMessage) }
        }
    }

    @Throws(IllegalStateException::class)
    override fun close(onError: (String) -> Unit) {
        awaitLock {
            relay.disconnect { errorMessage -> onError(errorMessage) }
        }
    }

    private fun <T> awaitLock(codeBlock: suspend () -> T): T {
        return runBlocking(signProtocolScope.coroutineContext) {
            mutex.withLock {
                checkEngineInitialization()
                codeBlock()
            }
        }
    }

    @Throws(IllegalStateException::class)
    internal fun checkEngineInitialization() {
        check(::signEngine.isInitialized) {
            "SignClient needs to be initialized first using the initialize function"
        }
    }
}