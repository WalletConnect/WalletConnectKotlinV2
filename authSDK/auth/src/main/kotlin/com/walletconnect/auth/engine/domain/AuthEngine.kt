@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import com.walletconnect.android_core.common.*
import com.walletconnect.android_core.common.SDKError
import com.walletconnect.android_core.common.model.*
import com.walletconnect.android_core.common.model.json_rpc.JsonRpcResponse
import com.walletconnect.android_core.common.model.sync.WCRequest
import com.walletconnect.android_core.common.model.sync.WCResponse
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.utils.DAY_IN_SECONDS
import com.walletconnect.android_core.utils.Logger
import com.walletconnect.auth.client.mapper.toDTO
import com.walletconnect.auth.common.exceptions.*
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestParamsException
import com.walletconnect.auth.common.exceptions.MissingIssuerException
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.json_rpc.params.AuthParams
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.RequesterDTO
import com.walletconnect.auth.common.model.CacaoVO
import com.walletconnect.auth.common.model.IssuerVO
import com.walletconnect.auth.common.model.PairingVO
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.engine.model.mapper.*
import com.walletconnect.auth.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.cacao.CacaoVerifier
import com.walletconnect.auth.storage.AuthStorageRepository
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.util.generateId
import com.walletconnect.utils.isSequenceValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

internal class AuthEngine(
    private val relayer: JsonRpcInteractor,
    private val crypto: KeyManagementRepository,
    private val storage: AuthStorageRepository,
    private val metaData: EngineDO.AppMetaData,
    private val issuer: IssuerVO?,
) {

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val authRequestMap: MutableMap<Long, WCRequest> = mutableMapOf()

    init {
        resubscribeToSequences()
        setupSequenceExpiration()
        collectJsonRpcRequests()
        collectJsonRpcResponses()
        collectInternalErrors()
    }


    internal fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun request(
        payloadParams: EngineDO.PayloadParams,
        onPairing: (EngineDO.Pairing) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        //todo: check if is authentication exists and is not expired
        // todo: do some magic


        //todo: check if is authentication exists and is  expired
        // todo: do some magic


        // For Alpha we are assuming not authenticated only todo: Remove comment after Alpha
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKey()
        val pairingTopic: Topic = crypto.getTopicFromKey(symmetricKey)
        crypto.setSymmetricKey(pairingTopic, symmetricKey)

        val relay = RelayProtocolOptions()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val inactivePairing = PairingVO(pairingTopic, relay, walletConnectUri.toAbsoluteString())

        try {
            storage.insertPairing(inactivePairing)
            val responsePublicKey: PublicKey = crypto.generateKeyPair()
            val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
            crypto.setSelfParticipant(responsePublicKey, responseTopic)

            val authParams: AuthParams.RequestParams = AuthParams.RequestParams(RequesterDTO(responsePublicKey.keyAsHex, metaData.toCore()), payloadParams.toDTO())
            val authRequest: AuthRpcDTO.AuthRequest = AuthRpcDTO.AuthRequest(generateId(), params = authParams)
            val irnParams = IrnParams(Tags.AUTH_REQUEST, Ttl(DAY_IN_SECONDS), true)
            relayer.publishJsonRpcRequests(pairingTopic, irnParams, authRequest,
                onSuccess = {
                    Logger.log("Auth request sent successfully on topic:$pairingTopic, awaiting response on topic:$responseTopic") // todo: Remove after Alpha
                    onPairing(EngineDO.Pairing(walletConnectUri.toAbsoluteString()))
                    relayer.subscribe(responseTopic)
                },
                onFailure = { error ->
                    Logger.error("Failed to send a auth request: $error")
                    onFailure(error)
                })

        } catch (e: SQLiteException) {
            crypto.removeKeys(pairingTopic.value)
            storage.deletePairing(pairingTopic)

            onFailure(e)
        }
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri =
            Validator.validateWCUri(uri) ?: throw MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (storage.isPairingValid(walletConnectUri.topic)) {
            throw PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val activePairing = PairingVO(walletConnectUri)
        val symmetricKey: SymmetricKey = walletConnectUri.symKey
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            relayer.subscribe(activePairing.topic)
            storage.insertPairing(activePairing)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            relayer.unsubscribe(activePairing.topic)
        }
    }

    internal fun respond(
        respond: EngineDO.Respond,
        onFailure: (Throwable) -> Unit,
    ) {
        if (authRequestMap[respond.id] == null) {
            onFailure(MissingAuthRequestException)
            return
        }
        val wcRequest: WCRequest = authRequestMap[respond.id]!!
        if (wcRequest.params !is AuthParams.RequestParams) {
            onFailure(MissingAuthRequestParamsException)
            return
        }
        val authParams: AuthParams.RequestParams = (wcRequest.params as AuthParams.RequestParams)
        val response: JsonRpcResponse = when (respond) {
            is EngineDO.Respond.Error -> JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
            is EngineDO.Respond.Result -> {
                val issuer: IssuerVO = issuer ?: throw MissingIssuerException
                val payload = authParams.payloadParams.toCacaoPayloadDTO(issuer)
                val cacao = CacaoDTO(CacaoDTO.HeaderDTO(SignatureType.EIP191.header), payload, respond.signature.toDTO())
                val responseParams = AuthParams.ResponseParams(cacao)

                if (!CacaoVerifier.verify(cacao.toVO())) throw InvalidCacaoException
                JsonRpcResponse.JsonRpcResult(respond.id, result = responseParams)
            }
        }

        val receiverPublicKey = PublicKey(authParams.requester.publicKey)
        val senderPublicKey: PublicKey = crypto.generateKeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
        crypto.setSymmetricKey(responseTopic, symmetricKey)

        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
        relayer.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = { Logger.log("Success Responded on topic: ${wcRequest.topic}") },
            onFailure = { Logger.error("Error Responded on topic: ${wcRequest.topic}") }
        )

    }

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        if (issuer != null) {
            scope.launch {
                authRequestMap[wcRequest.id] = wcRequest
                val formattedMessage: String = authParams.payloadParams.toFormattedMessage(issuer)
                _engineEvent.emit(EngineDO.Events.onAuthRequest(wcRequest.id, formattedMessage))
            }
        } else {
            val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
            relayer.respondWithError(wcRequest, PeerError.MissingIssuer, irnParams)
        }
    }

    private fun onAuthRequestResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                scope.launch {
                    _engineEvent.emit(EngineDO.Events.onAuthResponse(response.id, EngineDO.AuthResponse.Error(response.error.code, response.error.message)))
                }
            }
            is JsonRpcResponse.JsonRpcResult -> {
                val cacao: CacaoVO = (response.result as AuthParams.ResponseParams).cacao.toVO()
                if (CacaoVerifier.verify(cacao)) {
                    scope.launch {
                        _engineEvent.emit(EngineDO.Events.onAuthResponse(response.id, EngineDO.AuthResponse.Result(cacao)))
                    }
                } else {
                    scope.launch {
                        _engineEvent.emit(
                            EngineDO.Events.onAuthResponse(response.id, EngineDO.AuthResponse.Error(PeerError.SignatureVerificationFailed.code, PeerError.SignatureVerificationFailed.message))
                        )
                    }
                }
            }
        }
    }

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val params = request.params) {
                    is AuthParams.RequestParams -> onAuthRequest(request, params)
                }
            }
        }
    }


    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (response.params) {
                    is AuthParams.RequestParams -> onAuthRequestResponse(response)
                }
            }
        }
    }

    private fun resubscribeToSequences() {
        relayer.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { resubscribeToPairings() }
                }
            }
            .launchIn(scope)
    }

    private fun resubscribeToPairings() {
        val (listOfExpiredPairing, listOfValidPairing) =
            storage.getListOfPairingVOs().partition { pairing -> !pairing.expiry.isSequenceValid() }

        listOfExpiredPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic ->
                relayer.unsubscribe(pairingTopic)
                crypto.removeKeys(pairingTopic.value)
                storage.deletePairing(pairingTopic)
            }

        listOfValidPairing
            .map { pairing -> pairing.topic }
            .onEach { pairingTopic -> relayer.subscribe(pairingTopic) }
    }

    private fun setupSequenceExpiration() {
        storage.onPairingExpired = { topic ->
            relayer.unsubscribe(topic)
            crypto.removeKeys(topic.value)
        }
    }

    private fun collectInternalErrors() {
        relayer.internalErrors
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)
    }
}