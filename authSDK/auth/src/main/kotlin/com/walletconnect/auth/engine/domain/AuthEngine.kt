@file:JvmSynthetic

package com.walletconnect.auth.engine.domain

import android.database.sqlite.SQLiteException
import com.walletconnect.android_core.common.*
import com.walletconnect.android_core.common.model.*
import com.walletconnect.android_core.common.model.sync.WCRequest
import com.walletconnect.android_core.common.model.sync.WCResponse
import com.walletconnect.android_core.common.model.type.EngineEvent
import com.walletconnect.android_core.common.model.type.enums.EnvelopeType
import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.json_rpc.model.JsonRpcResponse
import com.walletconnect.android_core.utils.ACTIVE_PAIRING
import com.walletconnect.android_core.utils.DAY_IN_SECONDS
import com.walletconnect.android_core.utils.Logger
import com.walletconnect.auth.common.exceptions.*
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.*
import com.walletconnect.auth.engine.mapper.*
import com.walletconnect.auth.engine.mapper.toAbsoluteString
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.engine.mapper.toCore
import com.walletconnect.auth.engine.mapper.toFormattedMessage
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
    private val metaData: AppMetaData,
    private val issuer: Issuer?,
) {

    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

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
        payloadParams: PayloadParams,
        onPairing: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        // For Alpha we are assuming not authenticated only todo: Remove comment after Alpha
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKey()
        val pairingTopic: Topic = crypto.getTopicFromKey(symmetricKey)
        crypto.setSymmetricKey(pairingTopic, symmetricKey)

        val relay = RelayProtocolOptions()
        val walletConnectUri = WalletConnectUri(pairingTopic, symmetricKey, relay)
        val inactivePairing = Pairing(pairingTopic, relay, walletConnectUri.toAbsoluteString())

        try {
            storage.insertPairing(inactivePairing)
            val responsePublicKey: PublicKey = crypto.generateKeyPair()
            val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
            crypto.setSelfParticipant(responsePublicKey, responseTopic)

            val authParams: AuthParams.RequestParams =
                AuthParams.RequestParams(Requester(responsePublicKey.keyAsHex, metaData.toCore()), payloadParams)
            val authRequest: AuthRpc.AuthRequest = AuthRpc.AuthRequest(generateId(), params = authParams)
            val irnParams = IrnParams(Tags.AUTH_REQUEST, Ttl(DAY_IN_SECONDS), true)
            relayer.publishJsonRpcRequests(pairingTopic, irnParams, authRequest,
                onSuccess = {
                    Logger.log("Auth request sent successfully on topic:$pairingTopic, awaiting response on topic:$responseTopic") // todo: Remove after Alpha
                    onPairing(walletConnectUri.toAbsoluteString())
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
        val walletConnectUri: WalletConnectUri =
            Validator.validateWCUri(uri) ?: throw MalformedWalletConnectUri(MALFORMED_PAIRING_URI_MESSAGE)

        if (storage.isPairingValid(walletConnectUri.topic)) {
            throw PairWithExistingPairingIsNotAllowed(PAIRING_NOW_ALLOWED_MESSAGE)
        }

        val activePairing = Pairing(walletConnectUri)
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
        respond: Respond,
        onFailure: (Throwable) -> Unit,
    ) {
        val jsonRpcHistoryEntry = relayer.getPendingJsonRpcHistoryEntryById(respond.id)
        if (jsonRpcHistoryEntry == null) {
            Logger.error(MissingAuthRequestException.message)
            onFailure(MissingAuthRequestException)
            return
        }

        val authParams: AuthParams.RequestParams = jsonRpcHistoryEntry.params
        val response: JsonRpcResponse = when (respond) {
            is Respond.Error -> JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
            is Respond.Result -> {
                val issuer: Issuer = issuer ?: throw MissingIssuerException
                val payload: Cacao.Payload = authParams.payloadParams.toCacaoPayload(issuer)
                val cacao = Cacao(Cacao.Header(SignatureType.EIP191.header), payload, respond.signature.toCommon())
                val responseParams = AuthParams.ResponseParams(cacao)

                if (!CacaoVerifier.verify(cacao)) throw InvalidCacaoException
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
            onSuccess = { Logger.log("Success Responded on topic: $responseTopic") },
            onFailure = { Logger.error("Error Responded on topic: $responseTopic") }
        )

    }

    internal fun getResponseById(id: Long): Response? {
        return relayer.getResponseById(id)?.let { response ->
            when (response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val cacao: Cacao = (response.result as AuthParams.ResponseParams).cacao
                    Response.Result(response.id, cacao)
                }
                is JsonRpcResponse.JsonRpcError -> Response.Error(response.id, response.error.code, response.error.message)
            }
        }
    }

    internal fun getPendingRequests(): List<PendingRequest> {
        if (issuer == null) {
            throw MissingIssuerException
        }
        return relayer.getPendingJsonRpcHistoryEntries()
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest(issuer) }
    }

    private fun onAuthRequest(wcRequest: WCRequest, authParams: AuthParams.RequestParams) {
        if (issuer != null) {
            scope.launch {
                val formattedMessage: String = authParams.payloadParams.toFormattedMessage(issuer)
                _engineEvent.emit(Events.OnAuthRequest(wcRequest.id, formattedMessage))
            }
        } else {
            val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
            relayer.respondWithError(wcRequest, PeerError.MissingIssuer, irnParams)
        }
    }

    private fun onAuthRequestResponse(wcResponse: WCResponse) {
        val pairingTopic = wcResponse.topic
        if (!storage.isPairingValid(pairingTopic)) return
        val pairing = storage.getPairingByTopic(pairingTopic)
        if (!pairing.isActive) {
            storage.activatePairing(pairingTopic, ACTIVE_PAIRING)
        }

        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                scope.launch {
                    _engineEvent.emit(Events.OnAuthResponse(response.id, AuthResponse.Error(response.error.code, response.error.message)))
                }
            }
            is JsonRpcResponse.JsonRpcResult -> {
                val cacao: Cacao = (response.result as AuthParams.ResponseParams).cacao
                if (CacaoVerifier.verify(cacao)) {
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