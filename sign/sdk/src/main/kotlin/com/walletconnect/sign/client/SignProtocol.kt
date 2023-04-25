@file:JvmSynthetic

package com.walletconnect.sign.client

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.model.mapper.toPairing
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

class SignProtocol : SignInterface {
    private lateinit var signEngine: SignEngine

    companion object {
        val instance = SignProtocol()
    }

    override fun initialize(init: Sign.Params.Init, onSuccess: () -> Unit, onError: (Sign.Model.Error) -> Unit) {
        // TODO: re-init scope
        try {
            wcKoinApp.modules(
                commonModule(),
                jsonRpcModule(),
                storageModule(),
                engineModule()
            )

            signEngine = wcKoinApp.koin.get()
            signEngine.setup()
            onSuccess()
        } catch (e: Exception) {
            onError(Sign.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        checkEngineInitialization()

        signEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.SessionProposalEvent -> delegate.onSessionProposal(event.proposal.toClientSessionProposal(), event.context.toClientSessionContext())
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
                is EngineDO.SessionExtend -> delegate.onSessionExtend(event.toClientActiveSession())
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
        connect: Sign.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        try {
            signEngine.proposeSession(
                connect.namespaces?.toMapOfEngineNamespacesRequired(),
                connect.optionalNamespaces?.toMapOfEngineNamespacesOptional(),
                connect.properties,
                connect.pairing.toPairing(), onSuccess
            ) { error -> onError(Sign.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(
        pair: Sign.Params.Pair,
        onSuccess: (Sign.Params.Pair) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        try {
            signEngine.pair(
                uri = pair.uri,
                onSuccess = { onSuccess(pair) },
                onFailure = { throwable -> onError(Sign.Model.Error(throwable)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun approveSession(approve: Sign.Params.Approve, onSuccess: (Sign.Params.Approve) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.approve(
                proposerPublicKey = approve.proposerPublicKey,
                sessionNamespaces = approve.namespaces.toMapOfEngineNamespacesSession(),
                onSuccess = { onSuccess(approve) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun rejectSession(reject: Sign.Params.Reject, onSuccess: (Sign.Params.Reject) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.reject(reject.proposerPublicKey, reject.reason, onSuccess = { onSuccess(reject) }) { error ->
                onError(Sign.Model.Error(error))
            }
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Deprecated(
        "The onSuccess callback has been replaced with a new callback that returns Sign.Model.SentRequest",
        replaceWith = ReplaceWith("this.request(request, onSuccessWithSentRequest, onError)", "com.walletconnect.sign.client")
    )
    @Throws(IllegalStateException::class)
    override fun request(
        request: Sign.Params.Request,
        onSuccess: (Sign.Params.Request) -> Unit,
        onSuccessWithSentRequest: (Sign.Model.SentRequest) -> Unit,
        onError: (Sign.Model.Error) -> Unit
    ) {
        checkEngineInitialization()
        try {
            signEngine.sessionRequest(
                request = request.toEngineDORequest(),
                onSuccess = { onSuccess(request) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun request(request: Sign.Params.Request, onSuccess: (Sign.Model.SentRequest) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.sessionRequest(
                request = request.toEngineDORequest(),
                onSuccess = { requestId -> onSuccess(request.toSentRequest(requestId)) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(response: Sign.Params.Response, onSuccess: (Sign.Params.Response) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.respondSessionRequest(
                topic = response.sessionTopic,
                jsonRpcResponse = response.jsonRpcResponse.toJsonRpcResponse(),
                onSuccess = { onSuccess(response) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun update(update: Sign.Params.Update, onSuccess: (Sign.Params.Update) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.sessionUpdate(
                topic = update.sessionTopic,
                namespaces = update.namespaces.toMapOfEngineNamespacesSession(),
                onSuccess = { onSuccess(update) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun extend(extend: Sign.Params.Extend, onSuccess: (Sign.Params.Extend) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.extend(
                topic = extend.topic,
                onSuccess = { onSuccess(extend) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun emit(emit: Sign.Params.Emit, onSuccess: (Sign.Params.Emit) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.emit(
                topic = emit.topic,
                event = emit.event.toEngineEvent(emit.chainId),
                onSuccess = { onSuccess(emit) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
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
    override fun disconnect(disconnect: Sign.Params.Disconnect, onSuccess: (Sign.Params.Disconnect) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            signEngine.disconnect(
                topic = disconnect.sessionTopic,
                onSuccess = { onSuccess(disconnect) },
                onFailure = { error -> onError(Sign.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Sign.Model.Error(error))
        }
    }
    @Throws(IllegalStateException::class)
    override fun getListOfActiveSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
    }

    @Throws(IllegalStateException::class)
    override fun getActiveSessionByTopic(topic: String): Sign.Model.Session? {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
            .find { session -> session.topic == topic }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
    }

    @Throws(IllegalStateException::class)
    override fun getSettledSessionByTopic(topic: String): Sign.Model.Session? {
        checkEngineInitialization()
        return signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
            .find { session -> session.topic == topic }
    }

    @Deprecated(
        "Getting a list of Pairings will be moved to CoreClient to make pairing SDK agnostic",
        replaceWith = ReplaceWith("CoreClient.Pairing.getPairings()", "com.walletconnect.android.CoreClient")
    )
    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
        checkEngineInitialization()
        return signEngine.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing)
    }

    @Deprecated(
        "The return type of getPendingRequests methods has been replaced with SessionRequest list",
        replaceWith = ReplaceWith("getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>")
    )
    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        checkEngineInitialization()
        return signEngine.getPendingRequests(Topic(topic)).mapToPendingRequests()
    }

    @Throws(IllegalStateException::class)
    override fun getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest> {
        checkEngineInitialization()
        return signEngine.getPendingSessionRequests(Topic(topic)).mapToPendingSessionRequests()
    }

    @Throws(IllegalStateException::class)
    override fun getSessionProposals(): List<Sign.Model.SessionProposal> {
        checkEngineInitialization()
        return signEngine.getSessionProposals().map(EngineDO.SessionProposal::toClientSessionProposal)
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