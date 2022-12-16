@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android.Core
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.utils.CoreValidator
import com.walletconnect.android.impl.utils.DAY_IN_SECONDS
import com.walletconnect.android.impl.utils.MONTH_IN_SECONDS
import com.walletconnect.android.impl.utils.SELF_PARTICIPANT_CONTEXT
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.Cacao
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.InvalidParamsException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.*
import com.walletconnect.auth.engine.mapper.toCAIP122Message
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.engine.mapper.toPendingRequest
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.signature.CacaoType
import com.walletconnect.auth.signature.cacao.CacaoVerifier
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class AuthEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingJsonRpcHistoryEntriesUseCase: GetPendingJsonRpcHistoryEntriesUseCase,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
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
        topic: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val responsePublicKey: PublicKey = crypto.generateKeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
        val authParams: AuthParams.RequestParams = AuthParams.RequestParams(Requester(responsePublicKey.keyAsHex, selfAppMetaData), payloadParams)
        val authRequest: AuthRpc.AuthRequest = AuthRpc.AuthRequest(generateId(), params = authParams)
        val irnParams = IrnParams(Tags.AUTH_REQUEST, Ttl(DAY_IN_SECONDS), true)
        val pairingTopic = Topic(topic)
        crypto.setKey(responsePublicKey, "${SELF_PARTICIPANT_CONTEXT}${responseTopic.value}")

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
            },
            onFailure = { error ->
                logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
        )
    }

    internal fun respond(
        respond: Respond,
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
        val senderPublicKey: PublicKey = crypto.generateKeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
        crypto.setKey(symmetricKey, responseTopic.value)

        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
        jsonRpcInteractor.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = { logger.log("Success Responded on topic: $responseTopic") },
            onFailure = { logger.error("Error Responded on topic: $responseTopic") }
        )
    }

    internal fun formatMessage(payloadParams: PayloadParams, iss: String): String {
        val issuer = Issuer(iss)
        if (issuer.chainId != payloadParams.chainId) throw InvalidParamsException("Issuer chaiId does not match with PayloadParams")
        if (!CoreValidator.isChainIdCAIP2Compliant(payloadParams.chainId)) throw InvalidParamsException("PayloadParams chainId is not CAIP-2 compliant")
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw InvalidParamsException("Issuer address is not CAIP-10 compliant")

        return payloadParams.toCAIP122Message(issuer)
    }

    internal fun getPendingRequests(): List<PendingRequest> {
        return getPendingJsonRpcHistoryEntriesUseCase()
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest() }
    }

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        scope.launch {
            _engineEvent.emit(Events.OnAuthRequest(wcRequest.id, authParams.payloadParams))
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
            scope.launch { _engineEvent.emit(SDKError(InternalError(e))) }
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
            .onEach { request ->
                logger.error("kobe; Auth request: $request")
                onAuthRequest(request, request.params as AuthParams.RequestParams)
            }
            .launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is AuthParams }
            .onEach { response ->
                logger.error("kobe; Auth response: $response")
                onAuthRequestResponse(response, response.params as AuthParams.RequestParams)
            }
            .launchIn(scope)

    private fun resubscribeToPendingRequestsTopics() {
        pairingTopicToResponseTopicMap
            .map { it.value }
            .onEach { responseTopic: Topic ->
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error ->
                        scope.launch {
                            _engineEvent.emit(SDKError(InternalError(error)))
                        }
                    }
                } catch (e: Exception) {
                    scope.launch {
                        _engineEvent.emit(SDKError(InternalError(e)))
                    }
                }
            }
    }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
}