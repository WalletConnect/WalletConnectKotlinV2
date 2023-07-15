@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.InvalidParamsException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.AuthResponse
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.common.model.Requester
import com.walletconnect.auth.common.model.Respond
import com.walletconnect.auth.engine.mapper.toCAIP122Message
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.engine.mapper.toPendingRequest
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod.WC_AUTH_REQUEST
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.Date
import java.util.concurrent.TimeUnit

internal class AuthEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingJsonRpcHistoryEntriesUseCase: GetPendingJsonRpcHistoryEntriesUseCase,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val serializer: JsonRpcSerializer,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val cacaoVerifier: CacaoVerifier,
    private val logger: Logger
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    // idea: If we need responseTopic persistence throughout app terminations this is not sufficient. Decide after Alpha
    private val pairingTopicToResponseTopicMap: MutableMap<Topic, Topic> = mutableMapOf()

    init {
        pairingHandler.register(JsonRpcMethod.WC_AUTH_REQUEST)
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) { resubscribeToPendingRequestsTopics() }
                }

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }
                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectJsonRpcResponses()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    internal fun request(
        payloadParams: PayloadParams,
        expiry: Expiry? = null,
        topic: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val nowInSeconds = TimeUnit.SECONDS.convert(Date().time, TimeUnit.SECONDS)
        if (!CoreValidator.isExpiryWithinBounds(expiry ?: Expiry(300))) {
            return onFailure(InvalidExpiryException())
        }

        val responsePublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)

        val authParams: AuthParams.RequestParams = AuthParams.RequestParams(Requester(responsePublicKey.keyAsHex, selfAppMetaData), payloadParams, expiry)
        val authRequest: AuthRpc.AuthRequest = AuthRpc.AuthRequest(params = authParams)
        val irnParamsTtl = expiry?.run {
            val defaultTtl = DAY_IN_SECONDS
            val extractedTtl = seconds - nowInSeconds
            val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl

            Ttl(newTtl)
        } ?: Ttl(DAY_IN_SECONDS)
        val irnParams = IrnParams(Tags.AUTH_REQUEST, irnParamsTtl, true)
        val pairingTopic = Topic(topic)
        val requestTtlInSeconds = expiry?.run {
            seconds - nowInSeconds
        } ?: DAY_IN_SECONDS
        crypto.setKey(responsePublicKey, responseTopic.getParticipantTag())

        jsonRpcInteractor.publishJsonRpcRequest(pairingTopic, irnParams, authRequest,
            onSuccess = {
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error ->
                        return@subscribe onFailure(error)
                    }
                } catch (e: Exception) {
                    return@publishJsonRpcRequest onFailure(e)
                }

                pairingTopicToResponseTopicMap[pairingTopic] = responseTopic
                onSuccess()

                scope.launch {
                    try {
                        withTimeout(TimeUnit.SECONDS.toMillis(requestTtlInSeconds)) {
                            jsonRpcInteractor.peerResponse
                                .filter { response -> response.response.id == authRequest.id }
                                .collect { cancel() }
                        }
                    } catch (e: TimeoutCancellationException) {
                        _engineEvent.emit(SDKError(e))
                    }
                }
            },
            onFailure = { error ->
                logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
        )
    }

    internal fun respond(
        respond: Respond,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(respond.id)

        if (jsonRpcHistoryEntry == null) {
            logger.error(MissingAuthRequestException.message)
            onFailure(MissingAuthRequestException)
            return
        }

        val authParams: AuthParams.RequestParams = jsonRpcHistoryEntry.params
        val response: JsonRpcResponse = when (respond) {
            is Respond.Error -> JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
            is Respond.Result -> {
                val issuer = Issuer(respond.iss)
                val payload: Cacao.Payload = authParams.payloadParams.toCacaoPayload(issuer)
                val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, respond.signature.toCommon())
                val responseParams = CoreAuthParams.ResponseParams(cacao.header, cacao.payload, cacao.signature)
                if (!cacaoVerifier.verify(cacao)) throw InvalidCacaoException
                JsonRpcResponse.JsonRpcResult(respond.id, result = responseParams)
            }
        }

        val receiverPublicKey = PublicKey(authParams.requester.publicKey)
        val senderPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)

        authParams.expiry?.let { expiry ->
            if (!CoreValidator.isExpiryWithinBounds(expiry)) {
                scope.launch {
                    supervisorScope {
                        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
                        val wcRequest = WCRequest(responseTopic, respond.id, WC_AUTH_REQUEST, authParams)
                        jsonRpcInteractor.respondWithError(wcRequest, Invalid.RequestExpired, irnParams)
                    }
                }

                return onFailure(InvalidExpiryException())
            }
        }

        crypto.setKey(symmetricKey, responseTopic.value)

        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
        jsonRpcInteractor.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = {
                logger.log("Success Responded on topic: $responseTopic")
                scope.launch {
                    supervisorScope {
                        verifyContextStorageRepository.delete(respond.id)
                    }
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Error Responded on topic: $responseTopic")
                scope.launch {
                    supervisorScope {
                        verifyContextStorageRepository.delete(respond.id)
                    }
                }
                onFailure(error)
            }
        )
    }

    internal fun formatMessage(payloadParams: PayloadParams, iss: String): String {
        val issuer = Issuer(iss)
        if (issuer.chainId != payloadParams.chainId) throw InvalidParamsException("Issuer chainId does not match with PayloadParams")
        if (!CoreValidator.isChainIdCAIP2Compliant(payloadParams.chainId)) throw InvalidParamsException("PayloadParams chainId is not CAIP-2 compliant")
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw InvalidParamsException("Issuer address is not CAIP-10 compliant")

        return payloadParams.toCAIP122Message(issuer)
    }

    internal fun getPendingRequests(): List<PendingRequest> {
        return getPendingJsonRpcHistoryEntriesUseCase()
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest() }
    }

    internal suspend fun getVerifyContext(id: Long): VerifyContext? = verifyContextStorageRepository.get(id)
    internal suspend fun getListOfVerifyContext(): List<VerifyContext> = verifyContextStorageRepository.getAll()

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
        try {
            if (!CoreValidator.isExpiryWithinBounds(authParams.expiry)) {
                jsonRpcInteractor.respondWithError(wcRequest, Invalid.RequestExpired, irnParams)
                return
            }

            val json = serializer.serialize(AuthRpc.AuthRequest(id = wcRequest.id, params = authParams)) ?: throw Exception("Error serializing session proposal")
            val url = authParams.requester.metadata.url
            resolveAttestationIdUseCase(wcRequest.id, json, url) { verifyContext ->
                scope.launch {
                    _engineEvent.emit(Events.OnAuthRequest(wcRequest.id, wcRequest.topic.value, authParams.payloadParams, verifyContext))
                }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                wcRequest,
                Uncategorized.GenericError("Cannot handle a auth request: ${e.message}, topic: ${wcRequest.topic}"),
                irnParams
            )
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun onAuthRequestResponse(wcResponse: WCResponse, requestParams: AuthParams.RequestParams) {
        try {
            val pairingTopic = wcResponse.topic
            updatePairing(pairingTopic, requestParams)
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return
            pairingTopicToResponseTopicMap.remove(pairingTopic)

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcError -> {
                    scope.launch {
                        _engineEvent.emit(Events.OnAuthResponse(response.id, AuthResponse.Error(response.error.code, response.error.message)))
                    }
                }
                is JsonRpcResponse.JsonRpcResult -> {
                    val (header, payload, signature) = (response.result as CoreAuthParams.ResponseParams)
                    val cacao = Cacao(header, payload, signature)
                    if (cacaoVerifier.verify(cacao)) {
                        scope.launch {
                            _engineEvent.emit(Events.OnAuthResponse(response.id, AuthResponse.Result(cacao)))
                        }
                    } else {
                        scope.launch {
                            _engineEvent.emit(
                                Events.OnAuthResponse(
                                    response.id,
                                    AuthResponse.Error(PeerError.SignatureVerificationFailed.code, PeerError.SignatureVerificationFailed.message)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun updatePairing(
        topic: Topic,
        requestParams: AuthParams.RequestParams
    ) {
        pairingHandler.updateExpiry(Core.Params.UpdateExpiry(topic.value, Expiry(MONTH_IN_SECONDS)))
        pairingHandler.updateMetadata(Core.Params.UpdateMetadata(topic.value, requestParams.requester.metadata.toClient(), AppMetaDataType.PEER))
        pairingHandler.activate(Core.Params.Activate(topic.value))
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is AuthParams.RequestParams }
            .onEach { request -> onAuthRequest(request, request.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is AuthParams }
            .onEach { response -> onAuthRequestResponse(response, response.params as AuthParams.RequestParams) }
            .launchIn(scope)

    private fun resubscribeToPendingRequestsTopics() {
        val responseTopics = pairingTopicToResponseTopicMap.map { (_, responseTopic) -> responseTopic.value }
        try {
            jsonRpcInteractor.batchSubscribe(responseTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)
}