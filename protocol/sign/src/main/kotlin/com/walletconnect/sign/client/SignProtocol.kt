@file:JvmSynthetic

package com.walletconnect.sign.client

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.model.mapper.toPairing
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.client.mapper.*
import com.walletconnect.sign.common.exceptions.SignClientAlreadyInitializedException
import com.walletconnect.sign.di.engineModule
import com.walletconnect.sign.di.signJsonRpcModule
import com.walletconnect.sign.di.storageModule
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesOptional
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication

class SignProtocol(private val koinApp: KoinApplication = wcKoinApp) : SignInterface {
    private lateinit var signEngine: SignEngine

    companion object {
        val instance = SignProtocol()
    }

    override fun initialize(init: Sign.Params.Init, onSuccess: () -> Unit, onError: (Sign.Model.Error) -> Unit) {
        // TODO: re-init scope
        if (!::signEngine.isInitialized) {
            try {
                koinApp.modules(
                    signJsonRpcModule(),
                    storageModule(koinApp.koin.get<DatabaseConfig>().SIGN_SDK_DB_NAME),
                    engineModule()
                )

                signEngine = koinApp.koin.get()
                signEngine.setup()
                onSuccess()
            } catch (e: Exception) {
                onError(Sign.Model.Error(e))
            }
        } else {
            onError(Sign.Model.Error(SignClientAlreadyInitializedException()))
        }
    }

    @Throws(IllegalStateException::class)
    override fun setWalletDelegate(delegate: SignInterface.WalletDelegate) {
        checkEngineInitialization()

        signEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.SessionProposalEvent -> delegate.onSessionProposal(event.proposal.toClientSessionProposal(), event.context.toCore())
                is EngineDO.SessionAuthenticateEvent -> delegate.onSessionAuthenticate(event.toClientSessionAuthenticate(), event.verifyContext.toCore())
                is EngineDO.SessionRequestEvent -> delegate.onSessionRequest(event.request.toClientSessionRequest(), event.context.toCore())
                is EngineDO.SessionDelete -> delegate.onSessionDelete(event.toClientDeletedSession())
                is EngineDO.SessionExtend -> delegate.onSessionExtend(event.toClientActiveSession())
                //Responses
                is EngineDO.SettledSessionResponse -> delegate.onSessionSettleResponse(event.toClientSettledSessionResponse())
                is EngineDO.SessionUpdateNamespacesResponse -> delegate.onSessionUpdateResponse(event.toClientUpdateSessionNamespacesResponse())
                //Utils
                is EngineDO.ExpiredProposal -> delegate.onProposalExpired(event.toClient())
                is EngineDO.ExpiredRequest -> delegate.onRequestExpired(event.toClient())
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
                is EngineDO.SessionAuthenticateResponse -> delegate.onSessionAuthenticateResponse(event.toClientSessionAuthenticateResponse())
                //Utils
                is EngineDO.ExpiredProposal -> delegate.onProposalExpired(event.toClient())
                is EngineDO.ExpiredRequest -> delegate.onRequestExpired(event.toClient())
                is ConnectionState -> delegate.onConnectionStateChange(event.toClientConnectionState())
                is SDKError -> delegate.onError(event.toClientError())
            }
        }.launchIn(scope)
    }

    @Deprecated(
        message = "Replaced with the same name method but onSuccess callback returns a Pairing URL",
        replaceWith = ReplaceWith(expression = "fun connect(connect: Sign.Params.Connect, onSuccess: (String) -> Unit, onError: (Sign.Model.Error) -> Unit)")
    )
    @Throws(IllegalStateException::class)
    override fun connect(
        connect: Sign.Params.Connect,
        onSuccess: () -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        scope.launch {
            try {
                signEngine.proposeSession(
                    connect.namespaces?.toMapOfEngineNamespacesRequired(),
                    connect.optionalNamespaces?.toMapOfEngineNamespacesOptional(),
                    connect.properties,
                    connect.pairing.toPairing(),
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onError(Sign.Model.Error(error)) }
                )
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun connect(
        connect: Sign.Params.Connect,
        onSuccess: (String) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        scope.launch {

            try {
                signEngine.proposeSession(
                    connect.namespaces?.toMapOfEngineNamespacesRequired(),
                    connect.optionalNamespaces?.toMapOfEngineNamespacesOptional(),
                    connect.properties,
                    connect.pairing.toPairing(),
                    onSuccess = { onSuccess(connect.pairing.uri) },
                    onFailure = { error -> onError(Sign.Model.Error(error)) }
                )
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun sessionAuthenticate(
        authenticate: Sign.Params.Authenticate,
        onSuccess: (String) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        scope.launch {
            try {
                val pairing = signEngine.getPairingForSessionAuthenticate(authenticate.pairingTopic)
                val optionalNamespaces = signEngine.getNamespacesFromReCaps(authenticate.chains, authenticate.methods ?: emptyList()).toMapOfEngineNamespacesOptional()


                signEngine.authenticate(authenticate.toPayloadParams(), authenticate.methods, pairing.toPairing(),
                    onSuccess = { url ->
                        println("kobe: Auth Success")
                        onSuccess(url)
                    },
                    onFailure = { throwable -> onError(Sign.Model.Error(throwable)) })

                signEngine.proposeSession(
                    emptyMap(),
                    optionalNamespaces,
                    properties = null,
                    pairing = pairing.toPairing(),
                    onSuccess = {
                        println("kobe: Proposal Success")
                        /*Success*/
                    },
                    onFailure = { error -> onError(Sign.Model.Error(error)) }
                )
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun formatAuthMessage(formatMessage: Sign.Params.FormatMessage): String? {
        checkEngineInitialization()

        return try {
            runBlocking { signEngine.formatMessage(formatMessage.payloadParams.toCaip222Request(), formatMessage.iss) }
        } catch (error: Exception) {
            null
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(
        pair: Sign.Params.Pair,
        onSuccess: (Sign.Params.Pair) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun approveSession(approve: Sign.Params.Approve, onSuccess: (Sign.Params.Approve) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun rejectSession(reject: Sign.Params.Reject, onSuccess: (Sign.Params.Reject) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            try {
                signEngine.reject(reject.proposerPublicKey, reject.reason, onSuccess = { onSuccess(reject) }) { error ->
                    onError(Sign.Model.Error(error))
                }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun approveSessionAuthenticated(approve: Sign.Params.ApproveSessionAuthenticate, onSuccess: (Sign.Params.ApproveSessionAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            try {
                signEngine.approveSessionAuthenticate(
                    approve.id, approve.cacaos.toCommon(),
                    onSuccess = { onSuccess(approve) },
                    onFailure = { error -> onError(Sign.Model.Error(error)) }
                )

            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun rejectSessionAuthenticated(reject: Sign.Params.RejectSessionAuthenticate, onSuccess: (Sign.Params.RejectSessionAuthenticate) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            try {
                signEngine.rejectSessionAuthenticate(reject.id, reject.reason, onSuccess = { onSuccess(reject) }) { error -> onError(Sign.Model.Error(error)) }
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
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
        onError: (Sign.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun request(request: Sign.Params.Request, onSuccess: (Sign.Model.SentRequest) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun respond(response: Sign.Params.Response, onSuccess: (Sign.Params.Response) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun update(update: Sign.Params.Update, onSuccess: (Sign.Params.Update) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun extend(extend: Sign.Params.Extend, onSuccess: (Sign.Params.Extend) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun emit(emit: Sign.Params.Emit, onSuccess: (Sign.Params.Emit) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    @Throws(IllegalStateException::class)
    override fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing?) {
        checkEngineInitialization()

        scope.launch {
            try {
                signEngine.ping(
                    ping.topic,
                    { topic -> sessionPing?.onSuccess(Sign.Model.Ping.Success(topic)) },
                    { error -> sessionPing?.onError(Sign.Model.Ping.Error(error)) },
                    ping.timeout
                )
            } catch (error: Exception) {
                sessionPing?.onError(Sign.Model.Ping.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun disconnect(disconnect: Sign.Params.Disconnect, onSuccess: (Sign.Params.Disconnect) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
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
    }

    override fun decryptMessage(params: Sign.Params.DecryptMessage, onSuccess: (Sign.Model.Message) -> Unit, onError: (Sign.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            try {
                signEngine.decryptNotification(
                    topic = params.topic,
                    message = params.encryptedMessage,
                    onSuccess = { message ->
                        when (message) {
                            is Core.Model.Message.SessionRequest -> onSuccess(message.toSign())
                            is Core.Model.Message.SessionProposal -> onSuccess(message.toSign())
                            else -> {
                                //Ignore
                            }
                        }
                    },
                    onFailure = { error -> onError(Sign.Model.Error(error)) }
                )
            } catch (error: Exception) {
                onError(Sign.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfActiveSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return runBlocking {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
        }
    }

    @Throws(IllegalStateException::class)
    override fun getActiveSessionByTopic(topic: String): Sign.Model.Session? {
        checkEngineInitialization()
        return runBlocking {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
                .find { session -> session.topic == topic }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfSettledSessions(): List<Sign.Model.Session> {
        checkEngineInitialization()
        return runBlocking {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
        }
    }

    @Throws(IllegalStateException::class)
    override fun getSettledSessionByTopic(topic: String): Sign.Model.Session? {
        checkEngineInitialization()
        return runBlocking {
            signEngine.getListOfSettledSessions().map(EngineDO.Session::toClientActiveSession)
                .find { session -> session.topic == topic }
        }
    }

    @Deprecated(
        "Getting a list of Pairings will be moved to CoreClient to make pairing SDK agnostic",
        replaceWith = ReplaceWith("CoreClient.Pairing.getPairings()", "com.walletconnect.android.CoreClient")
    )
    @Throws(IllegalStateException::class)
    override fun getListOfSettledPairings(): List<Sign.Model.Pairing> {
        checkEngineInitialization()
        return runBlocking { signEngine.getListOfSettledPairings().map(EngineDO.PairingSettle::toClientSettledPairing) }
    }

    @Deprecated(
        "The return type of getPendingRequests methods has been replaced with SessionRequest list",
        replaceWith = ReplaceWith("getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest>")
    )
    @Throws(IllegalStateException::class)
    override fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest> {
        checkEngineInitialization()
        return runBlocking { signEngine.getPendingRequests(Topic(topic)).mapToPendingRequests() }
    }

    @Throws(IllegalStateException::class)
    override fun getPendingSessionRequests(topic: String): List<Sign.Model.SessionRequest> {
        checkEngineInitialization()
        return runBlocking { signEngine.getPendingSessionRequests(Topic(topic)).mapToPendingSessionRequests() }
    }

    @Throws(IllegalStateException::class)
    override fun getSessionProposals(): List<Sign.Model.SessionProposal> {
        checkEngineInitialization()
        return runBlocking { signEngine.getSessionProposals().map(EngineDO.SessionProposal::toClientSessionProposal) }
    }

    @Throws(IllegalStateException::class)
    override fun getVerifyContext(id: Long): Sign.Model.VerifyContext? {
        checkEngineInitialization()
        return runBlocking { signEngine.getVerifyContext(id)?.toCore() }
    }

    @Throws(IllegalStateException::class)
    override fun getListOfVerifyContexts(): List<Sign.Model.VerifyContext> {
        checkEngineInitialization()
        return runBlocking { signEngine.getListOfVerifyContexts().map { verifyContext -> verifyContext.toCore() } }
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