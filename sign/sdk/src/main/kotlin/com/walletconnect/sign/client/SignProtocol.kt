@file:JvmSynthetic

package com.walletconnect.sign.client

import com.walletconnect.android.RelayConnectionInterface
import com.walletconnect.android.common.wcKoinApp
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.impl.di.cryptoModule
import com.walletconnect.android.impl.di.networkModule
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.client.mapper.*
import com.walletconnect.sign.di.commonModule
import com.walletconnect.sign.di.engineModule
import com.walletconnect.sign.di.jsonRpcModule
import com.walletconnect.sign.di.storageModule
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.engine.model.EngineDO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SignProtocol : SignInterface {
    private lateinit var signEngine: SignEngine
    private lateinit var relay: RelayConnectionInterface

    companion object {
        val instance = SignProtocol()
        const val storageSuffix: String = ""
    }

    override fun initialize(initial: Sign.Params.Init, onError: (Sign.Model.Error) -> Unit) {
        with(initial) {
            // TODO: re-init scope
            // TODO: add logic to check hostName for ws/wss scheme with and without ://

            wcKoinApp.run {
                modules(
                    networkModule(relay),
                    commonModule(),
                    cryptoModule(),
                    jsonRpcModule(),
                    storageModule(storageSuffix),
                    engineModule(metadata)
                )
            }
        }

        signEngine = wcKoinApp.koin.get()
        signEngine.handleInitializationErrors { error -> onError(Sign.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        checkEngineInitialization()

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

    @Throws(IllegalStateException::class)
    override fun setDappDelegate(delegate: SignInterface.DappDelegate) {
        checkEngineInitialization()

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
            signEngine.reject(reject.proposerPublicKey, reject.reason) { error ->
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
            signEngine.respondSessionRequest(response.sessionTopic, response.jsonRpcResponse.toJsonRpcResponse()) { error ->
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
            signEngine.sessionUpdate(update.sessionTopic, update.namespaces.toMapOfEngineNamespacesSession())
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
    override fun getSettledSessionByTopic(topic: String): Sign.Model.Session? {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientSettledSession)
            .find { session -> session.topic == topic }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
        checkEngineInitialization()
        return signEngine.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        checkEngineInitialization()
        return signEngine.getPendingRequests(Topic(topic)).mapToPendingRequests()
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