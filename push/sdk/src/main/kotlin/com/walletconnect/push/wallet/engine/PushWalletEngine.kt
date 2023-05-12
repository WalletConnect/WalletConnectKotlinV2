@file:JvmSynthetic

package com.walletconnect.push.wallet.engine

import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.util.Base64
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.data.jwt.EncodePushAuthDidJwtPayloadUseCase
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.wallet.data.MessageRepository
import com.walletconnect.push.wallet.data.wellknown.config.PushConfigDTO
import com.walletconnect.push.wallet.data.wellknown.config.TypeDTO
import com.walletconnect.push.wallet.data.wellknown.did.DidJsonDTO
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit
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

    // TODO: Move to storage after deprecating old version of subscriptions
//    private val tempResponseTopicSubscribeTopicMap = mutableMapOf<Topic, Topic>()

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

    suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = coroutineScope {
        suspend fun createSubscription(dappPublicKey: PublicKey, dappScopes: List<TypeDTO>) {
            val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))
            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
            val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
//            tempResponseTopicSubscribeTopicMap[subscribeTopic] = responseTopic

            val (dappHomepageHost, dappListing) = withContext(Dispatchers.IO) {
                val listOfDappHomepages = runCatching {
                    explorerRepository.getAllDapps().listings.associateBy { listing -> listing.homepage.host }
                }.getOrDefault(emptyMap())

                listOfDappHomepages.entries.filter { (dappHomepageHost, dappListing) ->
                    dappHomepageHost != null && dappListing.description != null
                }.firstOrNull { (dappHomepageHost, _) ->
                    dappHomepageHost != null && dappHomepageHost.contains(dappUri.host!!)
                }
            } ?: return onFailure(IllegalArgumentException("Invalid dapp uri: $dappUri")).also {
                this.cancel()
            }

            val dappMetadata = AppMetaData(
                name = dappListing.name,
                description = dappListing.description!!,
                icons = listOf(dappListing.imageUrl.sm, dappListing.imageUrl.md, dappListing.imageUrl.lg),
                url = dappHomepageHost!!
            )

            val didJwt = registerIdentityAndReturnDidJwt(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
                return onFailure(error).also {
                    this.cancel()
                }
            }
            val params = PushParams.SubscribeParams(didJwt.value)
            val request = PushRpc.PushSubscribe(params = params)
            val irnParams = IrnParams(Tags.PUSH_SUBSCRIBE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.subscribe(responseTopic) { error ->
                return@subscribe onFailure(error)
            }

            jsonRpcInteractor.publishJsonRpcRequest(
                subscribeTopic,
                irnParams,
                request,
                envelopeType = EnvelopeType.ONE,
                participants = Participants(selfPublicKey, dappPublicKey),
                onSuccess = {
                    runBlocking {
                        subscriptionStorageRepository.insertSubscription(
                            request.id,
                            responseTopic.value,
                            null,
                            null,
                            account,
                            null,
                            null,
                            dappMetadata.name,
                            dappMetadata.description,
                            dappMetadata.url,
                            dappMetadata.icons,
                            dappListing.app.android,
                            didJwt.value,
                            dappScopes.associate { scope -> scope.name to Pair(scope.description, true) },
                            calcExpiry()
                        )
                    }

                    onSuccess()
                },
                onFailure = { error ->
                    return@publishJsonRpcRequest onFailure(error)
                }
            )
        }

        suspend fun extractPushConfig(): Result<List<TypeDTO>> = withContext(Dispatchers.IO) {
            val pushConfigDappUri = dappUri.run {
                if (this.path?.contains(".well-known/wc-push-config.json") == false) {
                    this.buildUpon().appendPath(".well-known/wc-push-config.json")
                } else {
                    this
                }
            }

            val wellKnownPushConfigString = URL(pushConfigDappUri.toString()).openStream().bufferedReader().use { it.readText() }
            val pushConfig = serializer.tryDeserialize<PushConfigDTO>(wellKnownPushConfigString) ?: return@withContext Result.failure(Exception("Failed to parse well-known/wc-push-config.json"))
            Result.success(pushConfig.types)
        }

        suspend fun extractDidJson(dappUri: Uri): Result<PublicKey> = withContext(Dispatchers.IO) {
            val didJsonDappUri = dappUri.run {
                if (this.path?.contains(".well-known/did.json") == false) {
                    this.buildUpon().appendPath(".well-known/did.json").build()
                } else {
                    this
                }
            }

            val wellKnownDidJsonString = URL(didJsonDappUri.toString()).openStream().bufferedReader().use { it.readText() }
            val didJson = serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse well-known/did.json"))
            val verificationKey = didJson.keyAgreement.first()
            val jwkPublicKey = didJson.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x
            val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
            val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()
            Result.success(PublicKey(publicKey))
        }

        val dappWellKnownProperties: Result<Pair<PublicKey, List<TypeDTO>>> = runCatching {
            extractDidJson(dappUri).getOrThrow() to extractPushConfig().getOrThrow()
        }

        dappWellKnownProperties.fold(
            onSuccess = { (dappPublicKey, dappScope) -> createSubscription(dappPublicKey, dappScope) },
            onFailure = { error -> return@coroutineScope onFailure(error) }
        )
    }

    suspend fun approve(requestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
        val dappPublicKey = PublicKey(respondedSubscription.peerPublicKeyAsHex)
        val responseTopic = sha256(dappPublicKey.keyAsBytes)

        val didJwt = registerIdentityAndReturnDidJwt(respondedSubscription.account, respondedSubscription.metadata.url, emptyList(), onSign, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }
        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
        val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(respondedSubscription.peerPublicKeyAsHex))
        val approvalParams = PushParams.RequestResponseParams(didJwt.value)
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.updateSubscriptionToRespondedByApproval(responseTopic, pushTopic.value, calcExpiry())

        jsonRpcInteractor.subscribe(pushTopic) { error ->
            return@subscribe onFailure(error)
        }
        jsonRpcInteractor.respondWithParams(
            respondedSubscription.requestId,
            Topic(responseTopic),
            approvalParams,
            irnParams,
            envelopeType = EnvelopeType.ONE,
            participants = Participants(selfPublicKey, dappPublicKey)
        ) { error ->
            return@respondWithParams onFailure(error)
        }

        onSuccess()
    }

    suspend fun update(topic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { subscription -> subscription.subscriptionTopic == topic }
            ?: return@supervisorScope onFailure(Exception("No subscription found for topic $topic"))
        val didJwt = registerIdentityAndReturnDidJwt(subscription.account, subscription.metadata.url, scopes, { null }, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }

        val updateParams = PushParams.UpdateParams(didJwt.value)
        val request = PushRpc.PushUpdate(params = updateParams)
        val irnParams = IrnParams(Tags.PUSH_UPDATE, Ttl(DAY_IN_SECONDS))
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
    }

    private suspend fun registerIdentityAndReturnDidJwt(
        account: AccountId,
        metadataUrl: String,
        scopes: List<String>,
        onSign: (String) -> Cacao.Signature?,
        onFailure: (Throwable) -> Unit,
    ): Result<DidJwt> {
        withContext(Dispatchers.IO) {
            identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
                onFailure(it)
                this.cancel()
            }
        }

        val joinedScope = scopes.joinToString(" ")
        val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(account)

        return encodeDidJwt(
            identityPrivateKey,
            EncodePushAuthDidJwtPayloadUseCase(metadataUrl, account, joinedScope),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
    }

    suspend fun reject(requestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = coroutineScope {
        try {
            val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithError(respondedSubscription.requestId, Topic(respondedSubscription.responseTopic), PeerError.Rejected.UserRejected(reason), irnParams) { error ->
                return@respondWithError onFailure(error)
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun deleteSubscription(topic: String, onFailure: (Throwable) -> Unit) = coroutineScope {
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
            .filter { subscription -> subscription.subscriptionTopic.isNullOrBlank().not() }
            .associateBy { subscription -> subscription.subscriptionTopic!! }

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

    private suspend fun collectJsonRpcRequests(): Job =
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
            when (val responseParams = response.params) {
                is PushParams.DeleteParams -> onPushDeleteResponse()
                is PushParams.SubscribeParams -> onPushSubscribeResponse(response, responseParams)
                is PushParams.UpdateParams -> onPushUpdateResponse(response, responseParams)
            }
        }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private suspend fun onPushRequest(request: WCRequest, params: PushParams.RequestParams) = supervisorScope {
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
//            scope.launch {
//                supervisorScope {

//                }
            subscriptionStorageRepository.insertSubscription(
                requestId = request.id,
                responseTopic = request.topic.value,
                peerPublicKeyAsHex = params.publicKey,
                subscriptionTopic = null,
                account = params.account,
                relayProtocol = null,
                relayData = null,
                name = params.metaData.name,
                description = params.metaData.description,
                url = params.metaData.url,
                icons = params.metaData.icons,
                native = params.metaData.redirect?.native,
                "",
                emptyMap(),
                calcExpiry()
            )

            _engineEvent.emit(params.toEngineDO(request.id, request.topic.value, RelayProtocolOptions()))
//            }
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

    private suspend fun onPushSubscribeResponse(wcResponse: WCResponse, pushSubscribeParams: PushParams.SubscribeParams) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { it.responseTopic == wcResponse.topic.value } ?: return@supervisorScope _engineEvent.emit(
                        SDKError(NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}"))
                    )
//                    val pushSubscriptionJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(pushSubscribeParams.subscriptionAuth).getOrElse { error ->
//                        _engineEvent.emit(SDKError(error))
//                        return@supervisorScope
//                    }
                    val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(Topic(subscription.responseTopic))
                    val dappPublicKey =
                        PublicKey((((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String)) // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)

                    // TODO: fetch dapp metadata using explorer api
                    // TODO: save push subscription auth in the database
//                    subscriptionStorageRepository.insertSubscription(
//                        requestId = wcResponse.response.id,
//                        pairingTopic = wcResponse.topic.value,
//                        peerPublicKeyAsHex = dappPublicKey.keyAsHex,
//                        subscriptionTopic = pushTopic.value,
//                        account = subscription.account,
//                        relayProtocol = null,
//                        relayData = null,
//                        name = "",
//                        description = "",
//                        url = pushSubscriptionJwtClaim.audience,
//                        icons = emptyList(),
//                        native = ""
//                    )

                    subscriptionStorageRepository.updateSubscriptionToResponded(subscription.responseTopic, pushTopic.value, dappPublicKey.keyAsHex, calcExpiry())

//                    scope.launch {
                    _engineEvent.emit(
                        subscription.copy(
                            subscriptionTopic = pushTopic.value,
                            peerPublicKeyAsHex = dappPublicKey.keyAsHex,
                        )
//                            EngineDO.PushSubscription(
//                                requestId = wcResponse.response.id,
//                                pairingTopic = wcResponse.topic.value,
//                                peerPublicKeyAsHex = dappPublicKey.keyAsHex,
//                                topic = pushTopic.value,
//                                account = subscription.account,
//                                relay = RelayProtocolOptions(),
//                                metadata = AppMetaData(
//                                    name = "",
//                                    description = "",
//                                    url = pushSubscriptionJwtClaim.audience,
//                                    icons = emptyList(),
//                                    redirect = null
//                                ),
//                                scope = subscription.scope,
//                                expiry =
//                            )
                    )
//                    }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _engineEvent.emit(EngineDO.PushSubscribeError(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _engineEvent.emit(SDKError(exception))
        }
    }

    private suspend fun onPushUpdateResponse(wcResponse: WCResponse, updateParams: PushParams.UpdateParams) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { it.subscriptionTopic == wcResponse.topic.value }
                        ?: throw NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val pushUpdateJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _engineEvent.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = pushUpdateJwtClaim.scope.split(" ")
                    val updateScopeMap: Map<String, Pair<String, Boolean>> = subscription.scope.entries.associate { (scopeName, descAndValue) ->
                        val (desc, value) = descAndValue
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to Pair(desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionStorageRepository.updateSubscriptionScopeAndJwt(wcResponse.topic.value, updateScopeMap, updateParams.subscriptionAuth, newExpiry)

                    _engineEvent.emit(
                        subscription.copy(
                            scope = updateScopeMap,
                            expiry = Expiry(newExpiry)
                        )
                    )
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _engineEvent.emit(EngineDO.PushUpdateError(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _engineEvent.emit(SDKError(exception))
        }
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }

    private fun calcExpiry(): Long {
        val currentTimeMs = System.currentTimeMillis()
        val currentTimeSeconds = TimeUnit.SECONDS.convert(currentTimeMs, TimeUnit.MILLISECONDS)
        val expiryTimeSeconds = currentTimeSeconds + MONTH_IN_SECONDS

        return Expiry(expiryTimeSeconds).seconds
    }
}