@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android_core.common.*
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
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.json_rpc.params.AuthParams
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.RequesterDTO
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
import kotlinx.coroutines.launch

internal class AuthEngine(
    private val relayer: JsonRpcInteractor,
    private val crypto: KeyManagementRepository,
    private val storage: AuthStorageRepository,
    private val metaData: EngineDO.AppMetaData,
    private val issuer: EngineDO.Issuer?,
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

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val params = request.params) {
                    is AuthParams.RequestParams -> onAuthRequest(request, params)
                }
            }
        }
    }

    //B receives request and decrypts it with symKey S from URI.
    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        if (issuer != null) {
            scope.launch {
                authRequestMap[wcRequest.id] = wcRequest
                //B constructs message to be signed from request and signs it.
                val formattedMessage: String = authParams.payloadParams.toCacaoPayloadDTO(issuer).toEngineDO().toFormattedMessage() //todo: Add mapper
                _engineEvent.emit(EngineDO.Events.onAuthRequest(wcRequest.id, formattedMessage))
            }
        } else {
            //TODO: inform Peer A that something went wrong
//            relayer.respondWithError(request, PeerError.SessionSettlementFailed(NAMESPACE_MISSING_PROPOSAL_MESSAGE), irnParams)
            Logger.error("Missing issuer")
        }
    }

    private fun collectJsonRpcResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is AuthParams.RequestParams -> onAuthRequestResponse(response)
                }
            }
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
                val cacao: EngineDO.Cacao = (response.result as AuthParams.ResponseParams).cacao.toEngineDO()
                //A receives response and validates signature.
                if (CacaoVerifier.verify(cacao)) {
                    scope.launch {
                        //If signature is valid, then user is authenticated.
                        _engineEvent.emit(EngineDO.Events.onAuthResponse(response.id, EngineDO.AuthResponse.Result(cacao)))
                    }
                } else {
                    scope.launch {
                        //todo: Define Error Codes
                        //If signature is not valid, then throw verification error.
                        _engineEvent.emit(EngineDO.Events.onAuthResponse(response.id, EngineDO.AuthResponse.Error(11004, "Signature Verification Failed")))
                        //todo: Protocol Improvement: Maybe notify Peer B about wrong verification?
                    }
                }
            }
        }
    }

    internal fun pair(uri: String) {
        val walletConnectUri: EngineDO.WalletConnectUri =
            Validator.validateWCUri(uri) ?: throw MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (storage.isPairingValid(walletConnectUri.topic)) {
            throw PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val activePairing = PairingVO(walletConnectUri).also { Logger.log("Responder pairingTopic:${it.topic}") }
        val symmetricKey = walletConnectUri.symKey.also { Logger.log("Responder symKey:${it.keyAsHex}") }
        crypto.setSymmetricKey(walletConnectUri.topic, symmetricKey)

        try {
            //B subscribes to pairing topic from provided URI
            relayer.subscribe(activePairing.topic)
            storage.insertPairing(activePairing)
        } catch (e: SQLiteException) {
            crypto.removeKeys(walletConnectUri.topic.value)
            relayer.unsubscribe(activePairing.topic)
        }
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        relayer.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun respond(
        respond: EngineDO.Respond,
        onFailure: (Throwable) -> Unit,
    ) {
        if (authRequestMap[respond.id] == null) {
            Logger.error("Missing Auth Request")
            onFailure(Throwable("Missing Auth Request"))
            return
        }
        val wcRequest: WCRequest = authRequestMap[respond.id]!!
        if (wcRequest.params !is AuthParams.RequestParams) {
            Logger.error("Missing Auth Request Params")
            onFailure(Throwable("Missing Auth Request Params"))
            return
        }
        val authParams: AuthParams.RequestParams = (wcRequest.params as AuthParams.RequestParams)
        val response: JsonRpcResponse = when (respond) {
            is EngineDO.Respond.Error -> {
                JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
            }
            is EngineDO.Respond.Result -> {

                val payload = authParams.payloadParams.toCacaoPayloadDTO(issuer!!)
                val cacao = CacaoDTO(CacaoDTO.HeaderDTO(SignatureType.EIP191.header), payload, respond.signature.toDTO())
                val responseParams = AuthParams.ResponseParams(cacao)
                CacaoVerifier.verify(cacao.toEngineDO()).let { Logger.log("Is Cacao valid: $it") } // todo: Add onFailure/throw when cacao is not valid
                JsonRpcResponse.JsonRpcResult(respond.id, result = responseParams)
            }
        }

        //B generates keyPair Y and generates shared symKey R.
        val receiverPublicKey = PublicKey(authParams.requester.publicKey) // PubKey X
        val senderPublicKey: PublicKey = crypto.generateKeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey) // SymKey R
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
        crypto.setSymmetricKey(responseTopic, symmetricKey)
        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)

        //B encrypts response with symKey R as type 1 envelope.
        //B sends Cacao response on response topic.
        relayer.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = { Logger.log("Success Responded on topic: ${wcRequest.topic}") },
            onFailure = { Logger.error("Error Responded on topic: ${wcRequest.topic}") }
        )

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


        // todo: ATM assuming not authenticated
        //A creates random symKey S for pairing topic.
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKey().also { Logger.log("Requester symKey:${it.keyAsHex}") }
        //Pairing topic is the hash of symKey S.
        val pairingTopic: Topic = crypto.getTopicFromKey(symmetricKey).also { Logger.log("Requester pairingTopic:$it") }
        crypto.setSymmetricKey(pairingTopic, symmetricKey)
        //todo: refactor to not use crypto 3 times
        val relay = RelayProtocolOptions()
        val walletConnectUri = EngineDO.WalletConnectUri(pairingTopic, symmetricKey, relay)
        val inactivePairing = PairingVO(pairingTopic, relay, walletConnectUri.toAbsoluteString())

        try {
            storage.insertPairing(inactivePairing)
            //A generates keyPair X and generates response topic.
            val responsePublicKey: PublicKey = crypto.generateKeyPair()
            //Response topic is the hash of publicKey X.
            val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
            crypto.setSelfParticipant(responsePublicKey, responseTopic) // For future decrypting of EnvelopeType.ZERO
            //A will construct an authentication request.
            val authParams: AuthParams.RequestParams = AuthParams.RequestParams(RequesterDTO(responsePublicKey.keyAsHex, metaData.toCore()), payloadParams.toDTO())
            val authRequest: AuthRpcDTO.AuthRequest = AuthRpcDTO.AuthRequest(generateId(), params = authParams)
            val irnParams = IrnParams(Tags.AUTH_REQUEST, Ttl(DAY_IN_SECONDS), true)
            //A encrypts request with symKey S.
            //A publishes encrypted request to topic.
            relayer.publishJsonRpcRequests(pairingTopic, irnParams, authRequest,
                onSuccess = {
                    Logger.log("Auth request sent successfully")
                    onPairing(EngineDO.Pairing(walletConnectUri.toAbsoluteString()))
                    //A subscribes to messages on response topic.
                    relayer.subscribe(responseTopic).also { Logger.log("Responder responseTopic:${responseTopic}") }
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