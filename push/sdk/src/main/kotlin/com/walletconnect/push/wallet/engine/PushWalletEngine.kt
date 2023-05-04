@file:JvmSynthetic

package com.walletconnect.push.wallet.engine

import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.data.jwt.EncodePushAuthDidJwtPayloadUseCase
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.wallet.data.MessageRepository
import com.walletconnect.push.wallet.data.wellknown.DidJsonDTO
import com.walletconnect.util.generateId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URL
import kotlin.reflect.full.safeCast

internal class PushWalletEngine(
    private val keyserverUrl: String,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val messageRepository: MessageRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val serializer: JsonRpcSerializer,
    private val explorerRepository: ExplorerRepository,
    private val logger: Logger,
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE
        )
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        resubscribeToSubscriptions()
                    }
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

    suspend fun subscribeToDapp(dappURL: URL, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        suspend fun createSubscription(dappPublicKey: PublicKey) {
            val responseTopic = Topic(sha256(dappPublicKey.keyAsBytes))
            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
            val subscribeTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)

            jsonRpcInteractor.subscribe(subscribeTopic) { error ->
                onFailure(error)
                this@supervisorScope.cancel()
            }

//            val dappMetadata = withContext(Dispatchers.IO) {
//                explorerRepository.getAllDapps().listings.values.firstOrNull { it.metadata.url == dappURL }?.let { dapp ->
//                    dappMetadata.copy(
//                        name = dapp.name,
//                        description = dapp.description,
//                        icons = dapp.icons,
//                        url = dapp.url
//                    )
//                } ?: dappMetadata
//            }

            val didJwt = registerIdentityAndReturnDidJwt(AccountId(account), dappURL.toString(), onSign, onFailure).getOrElse { error ->
                return onFailure(error).also {
                    this.cancel()
                }
            }
            val params = PushParams.SubscribeParams(didJwt.value)
            val request = PushRpc.PushSubscribe(params = params)
            val irnParams = IrnParams(Tags.PUSH_SUBSCRIBE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.subscribe(subscribeTopic) { error ->
                return@subscribe onFailure(error)
            }
            jsonRpcInteractor.publishJsonRpcRequest(
                responseTopic,
                irnParams,
                request,
                envelopeType = EnvelopeType.ONE,
                participants = Participants(selfPublicKey, dappPublicKey)
            ) { error ->
                return@publishJsonRpcRequest onFailure(error)
            }

            onSuccess()
        }

        val potentialDappPublicKey: Result<PublicKey> = withContext(Dispatchers.IO) {
            if (!dappURL.toString().endsWith(".well-known/did.json")) {
                return@withContext Result.failure(Exception("Failed to fetch dapp's DID doc from $dappURL/.well-known/did.json"))
            }

            val wellKnownDidJsonString = dappURL.openStream().bufferedReader().use { it.readText() }
            val didJson = serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse well-known/did.json"))
            val verificationKey = didJson.keyAgreement.first()
            val publicKey = didJson.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x
            return@withContext Result.success(
                PublicKey(publicKey)
            )
        }

        potentialDappPublicKey.fold(
            onSuccess = { dappPublicKey -> createSubscription(dappPublicKey) },
            onFailure = { error -> return@supervisorScope onFailure(error) }
        )
    }

    suspend fun approve(requestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
        val peerPublicKey = PublicKey(respondedSubscription.peerPublicKeyAsHex)
        val responseTopic = sha256(peerPublicKey.keyAsBytes)

        val didJwt = registerIdentityAndReturnDidJwt(respondedSubscription.account, respondedSubscription.metadata.url, onSign, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error).also {
                this.cancel()
            }
        }
        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
        val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(respondedSubscription.peerPublicKeyAsHex))
        val approvalParams = PushParams.RequestResponseParams(didJwt.value)
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.updateSubscriptionToResponded(requestId, pushTopic.value, respondedSubscription.metadata)

        jsonRpcInteractor.subscribe(pushTopic) { error ->
            return@subscribe onFailure(error)
        }
        jsonRpcInteractor.respondWithParams(
            respondedSubscription.requestId,
            Topic(responseTopic),
            approvalParams,
            irnParams,
            envelopeType = EnvelopeType.ONE,
            participants = Participants(selfPublicKey, peerPublicKey)
        ) { error ->
            return@respondWithParams onFailure(error)
        }

        onSuccess()
    }

    private suspend fun registerIdentityAndReturnDidJwt(
        account: AccountId,
        metadataUrl: String,
        onSign: (String) -> Cacao.Signature?,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> {
        withContext(Dispatchers.IO) {
            identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
                onFailure(it)
                this.cancel()
            }
        }

        val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(account)

        return encodeDidJwt(
            identityPrivateKey,
            EncodePushAuthDidJwtPayloadUseCase(metadataUrl, account),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    suspend fun reject(requestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithError(respondedSubscription.requestId, Topic(respondedSubscription.pairingTopic), PeerError.Rejected.UserRejected(reason), irnParams) { error ->
                return@respondWithError onFailure(error)
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun deleteSubscription(topic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
        val deleteParams = PushParams.DeleteParams(6000, "User Disconnected")
        val request = PushRpc.PushDelete(id = generateId(), params = deleteParams)
        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.deleteSubscription(topic)

        jsonRpcInteractor.unsubscribe(Topic(topic))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request,
            onSuccess = {
                CoreClient.Echo.unregister({
                    logger.log("Delete sent successfully")
                }, {
                    onFailure(it)
                })
            },
            onFailure = {
                onFailure(it)
            }
        )
    }

    fun deleteMessage(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            messageRepository.deleteMessage(requestId)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.PushMessage) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            val codec = wcKoinApp.koin.get<Codec>()
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            // How to look in JsonRpcHistory for dupes without Rpc ID
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return onFailure(IllegalArgumentException("Unable to deserialize message"))
            val pushMessage = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)
            val pushMessageEngineDO = PushParams.MessageParams::class.safeCast(pushMessage)?.toEngineDO() ?: return onFailure(IllegalArgumentException("Unable to deserialize message"))

            onSuccess(pushMessageEngineDO)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription> =
        subscriptionStorageRepository.getAllSubscriptions()
            .filter { subscription -> subscription.topic.isNullOrBlank().not() }
            .associateBy { subscription -> subscription.topic!! }

    fun getListOfMessages(topic: String): Map<Long, EngineDO.PushRecord> =
        messageRepository.getMessagesByTopic(topic).map { messageRecord ->
            EngineDO.PushRecord(
                id = messageRecord.id,
                topic = messageRecord.topic,
                publishedAt = messageRecord.publishedAt,
                message = EngineDO.PushMessage(
                    title = messageRecord.message.title,
                    body = messageRecord.message.body,
                    icon = messageRecord.message.icon,
                    url = messageRecord.message.url,
                )
            )
        }.associateBy { pushRecord ->
            pushRecord.id
        }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PushParams.RequestParams -> onPushRequest(request, requestParams)
                    is PushParams.MessageParams -> onPushMessage(request, requestParams)
                    is PushParams.DeleteParams -> onPushDelete(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse.onEach { response ->
            when (response.params) {
                is PushParams.DeleteParams -> onPushDeleteResponse()
                is PushParams.SubscribeParams -> onPushSubscribeResponse(response)
            }
        }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun onPushRequest(request: WCRequest, params: PushParams.RequestParams) {
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            scope.launch {
                supervisorScope {
                    withContext(Dispatchers.IO) {
                        subscriptionStorageRepository.insertSubscription(
                            request.id,
                            request.topic.value,
                            params.publicKey,
                            null,
                            params.account,
                            null,
                            null,
                            params.metaData.name,
                            params.metaData.description,
                            params.metaData.url,
                            params.metaData.icons,
                            params.metaData.redirect?.native
                        )
                    }
                }

                _engineEvent.emit(params.toEngineDO(request.id, request.topic.value, RelayProtocolOptions()))
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }

    private fun onPushMessage(request: WCRequest, params: PushParams.MessageParams) {
        val irnParams = IrnParams(Tags.PUSH_MESSAGE_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            // TODO: refactor to use the RPC published at value 
            val currentTime = CURRENT_TIME_IN_SECONDS
            messageRepository.insertMessage(request.id, request.topic.value, currentTime, params.title, params.body, params.icon, params.url)
            val messageRecord = EngineDO.PushRecord(
                id = request.id,
                topic = request.topic.value,
                publishedAt = currentTime,
                message = params.toEngineDO()
            )
            scope.launch { _engineEvent.emit(messageRecord) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push message: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }

    private suspend fun onPushDelete(request: WCRequest) = supervisorScope {
        val irnParams = IrnParams(Tags.PUSH_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            jsonRpcInteractor.unsubscribe(request.topic)
            subscriptionStorageRepository.deleteSubscription(request.topic.value)

            scope.launch { _engineEvent.emit(EngineDO.PushDelete(request.topic.value)) }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(e)) }
        }
    }

    private fun onPushDeleteResponse() {
        // TODO: Review if we need this
    }

    private suspend fun onPushSubscribeResponse(wcResponse: WCResponse) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val pushSubscribeResponse = wcResponse.params as PushParams.SubscribeParams
                    val pushSubscriptionJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(pushSubscribeResponse.subscriptionAuth).getOrElse { error ->
                        _engineEvent.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(wcResponse.topic)
                    val dappPublicKey = decodeX25519DidKey(pushSubscriptionJwtClaim.issuer)
                    val subscribeTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
                    val account = if (pushSubscriptionJwtClaim.subject.contains(Regex(".:.:."))) {
                        pushSubscriptionJwtClaim.subject.split(":").last()
                    } else {
                        pushSubscriptionJwtClaim.subject
                    }

                    withContext(Dispatchers.IO) {
                        // TODO: fetch dapp metadata using explorer api
                        subscriptionStorageRepository.insertSubscription(
                            requestId = wcResponse.response.id,
                            pairingTopic = wcResponse.topic.value,
                            peerPublicKeyAsHex = dappPublicKey.keyAsHex,
                            subscriptionTopic = subscribeTopic.value,
                            account = account,
                            relayProtocol = RelayProtocolOptions().protocol,
                            relayData = RelayProtocolOptions().data,
                            name = "",
                            description = "",
                            url = pushSubscriptionJwtClaim.audience,
                            icons = emptyList(),
                            native = ""
                        )
                    }
                }
                is JsonRpcResponse.JsonRpcError -> {
                    scope.launch { _engineEvent.emit(EngineDO.PushSubscribeError(wcResponse.response.id, response.error.message)) }
                }
            }
        } catch (exception: Exception) {
            scope.launch { _engineEvent.emit(SDKError(exception)) }
        }
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }
}