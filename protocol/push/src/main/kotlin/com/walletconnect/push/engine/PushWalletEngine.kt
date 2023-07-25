@file:JvmSynthetic

package com.walletconnect.push.engine

import android.content.res.Resources.NotFoundException
import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toDb
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.data.MessagesRepository
import com.walletconnect.push.engine.calls.ApproveUseCaseInterface
import com.walletconnect.push.engine.calls.DecryptMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteMessageUseCaseInterface
import com.walletconnect.push.engine.calls.DeleteSubscriptionUseCaseInterface
import com.walletconnect.push.engine.calls.EnableSyncUseCaseInterface
import com.walletconnect.push.engine.calls.RejectUseCaseInterface
import com.walletconnect.push.engine.calls.SubscribeUseCaseInterface
import com.walletconnect.push.engine.calls.UpdateUseCaseInterface
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import com.walletconnect.push.engine.sync.use_case.events.OnSyncUpdateEventUseCase
import com.walletconnect.push.engine.sync.use_case.requests.SetSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class PushWalletEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val messagesRepository: MessagesRepository,
    private val enginePushSubscriptionNotifier: EnginePushSubscriptionNotifier,
    private val logger: Logger,
    private val syncClient: SyncInterface,
    private val onSyncUpdateEventUseCase: OnSyncUpdateEventUseCase,
    private val setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase: SetSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase,
    private val historyInterface: HistoryInterface,
    private val subscribeUserCase: SubscribeUseCaseInterface,
    private val approveUseCase: ApproveUseCaseInterface,
    private val rejectUserCase: RejectUseCaseInterface,
    private val updateUseCase: UpdateUseCaseInterface,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCaseInterface,
    private val deleteMessageUseCase: DeleteMessageUseCaseInterface,
    private val decryptMessageUseCase: DecryptMessageUseCaseInterface,
    private val enableSyncUseCase: EnableSyncUseCaseInterface,
) : SubscribeUseCaseInterface by subscribeUserCase,
    ApproveUseCaseInterface by approveUseCase,
    RejectUseCaseInterface by rejectUserCase,
    UpdateUseCaseInterface by updateUseCase,
    DeleteSubscriptionUseCaseInterface by deleteSubscriptionUseCase,
    DeleteMessageUseCaseInterface by deleteMessageUseCase,
    DecryptMessageUseCaseInterface by decryptMessageUseCase,
    EnableSyncUseCaseInterface by enableSyncUseCase {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private var syncUpdateEventsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE,
            JsonRpcMethod.WC_PUSH_DELETE,
            JsonRpcMethod.WC_PUSH_PROPOSE,
            JsonRpcMethod.WC_PUSH_SUBSCRIBE,
            JsonRpcMethod.WC_PUSH_UPDATE,
        )
    }

    suspend fun setup() {
        registerTagsInHistory()
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

                if (syncUpdateEventsJob == null) {
                    syncUpdateEventsJob = collectSyncUpdateEvents()
                }
            }
            .launchIn(scope)
    }

    private suspend fun registerTagsInHistory() {
        // Sync are here since History Server expects only one register call
        historyInterface.registerTags(tags = listOf(Tags.PUSH_MESSAGE, Tags.SYNC_SET, Tags.SYNC_DELETE), {}, { error -> logger.error(error.throwable) })
    }

//    suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (Long, DidJwt) -> Unit, onFailure: (Throwable) -> Unit) =
//        supervisorScope {
//            suspend fun createSubscription(dappPublicKey: PublicKey, dappScopes: List<EngineDO.PushScope.Remote>) {
//                // Subscribe topic is derived from the sha256 hash of public key X
//                val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))
//
//                // Protection against multiple subscription for the same account and dapp
//                if (subscriptionRepository.isAlreadyRequested(account, subscribeTopic.value)) return onFailure(IllegalStateException("Account: $account is already subscribed to dapp: $dappUri"))
//
//                // Wallet generates key pair Y
//                val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
//                // Wallet derives symmetric key S with keys X and Y
//                // Response topic is derived from the sha256 hash of symmetric key S
//                val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
//
//                val dappMetaData: AppMetaData = withContext(Dispatchers.IO) {
//                    // Fetch dapp metadata from explorer api
//                    val listOfDappHomepages = runCatching {
//                        explorerRepository.getAllDapps().listings.associateBy { listing -> listing.homepage }
//                    }.getOrElse { error ->
//                        return@withContext Result.failure(error)
//                    }
//
//                    // Find dapp metadata for dapp uri
//                    val (dappHomepageUri: Uri, dappListing: Listing) = listOfDappHomepages.entries.filter { (_, dappListing) ->
//                        dappListing.description != null
//                    }.firstOrNull { (dappHomepageUri, _) ->
//                        dappHomepageUri.host != null && dappHomepageUri.host!!.contains(dappUri.host!!)
//                    } ?: return@withContext Result.failure<AppMetaData>(IllegalArgumentException("Unable to find dapp listing for $dappUri"))
//
//                    // Return dapp metadata
//                    return@withContext Result.success(
//                        AppMetaData(
//                            name = dappListing.name,
//                            description = dappListing.description!!,
//                            icons = listOf(dappListing.imageUrl.sm, dappListing.imageUrl.md, dappListing.imageUrl.lg),
//                            url = dappHomepageUri.toString(),
//                            redirect = Redirect(dappListing.app.android)
//                        )
//                    )
//                }.getOrElse {
//                    return onFailure(it)
//                }
//
//                val didJwt = registerIdentityAndReturnDidJwt(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
//                    return onFailure(error)
//                }
//                val params = PushParams.SubscribeParams(didJwt.value)
//                val request = PushRpc.PushSubscribe(params = params)
//                val irnParams = IrnParams(Tags.PUSH_SUBSCRIBE, Ttl(DAY_IN_SECONDS))
//
//                runCatching {
//                    subscriptionRepository.insertOrAbortRequestedSubscription(
//                        requestId = request.id,
//                        subscribeTopic = subscribeTopic.value,
//                        responseTopic = responseTopic.value,
//                        account = account,
//                        mapOfScope = dappScopes.associate { scope -> scope.name to Pair(scope.description, true) },
//                        expiry = calcExpiry().seconds,
//                    )
//                }.mapCatching {
//                    metadataStorageRepository.insertOrAbortMetadata(
//                        topic = responseTopic,
//                        appMetaData = dappMetaData,
//                        appMetaDataType = AppMetaDataType.PEER,
//                    )
//                }.onFailure { error ->
//                    logger.error("Cannot insert: ${error.message}")
//                    return onFailure(error)
//                }
//
//                // Wallet subscribes to response topic
//                jsonRpcInteractor.subscribe(responseTopic) { error ->
//                    return@subscribe onFailure(error)
//                }
//
//                // Wallet sends push subscribe request (type 1 envelope) on subscribe topic with subscriptionAuth
//                jsonRpcInteractor.publishJsonRpcRequest(
//                    topic = subscribeTopic,
//                    params = irnParams,
//                    payload = request,
//                    envelopeType = EnvelopeType.ONE,
//                    participants = Participants(selfPublicKey, dappPublicKey),
//                    onSuccess = {
//                        onSuccess(request.id, didJwt)
//                    },
//                    onFailure = { error ->
//                        onFailure(error)
//                    }
//                )
//            }
//
//            suspend fun extractDidJson(dappUri: Uri): Result<PublicKey> = withContext(Dispatchers.IO) {
//                val didJsonDappUri = dappUri.run {
//                    if (this.path?.contains(DID_JSON) == false) {
//                        this.buildUpon().appendPath(DID_JSON).build()
//                    } else {
//                        this
//                    }
//                }
//
//                val wellKnownDidJsonString = URL(didJsonDappUri.toString()).openStream().bufferedReader().use { it.readText() }
//                val didJson = serializer.tryDeserialize<DidJsonDTO>(wellKnownDidJsonString) ?: return@withContext Result.failure(Exception("Failed to parse well-known/did.json"))
//                val verificationKey = didJson.keyAgreement.first()
//                val jwkPublicKey = didJson.verificationMethod.first { it.id == verificationKey }.publicKeyJwk.x
//                val replacedJwk = jwkPublicKey.replace("-", "+").replace("_", "/")
//                val publicKey = Base64.decode(replacedJwk, Base64.DEFAULT).bytesToHex()
//                Result.success(PublicKey(publicKey))
//            }
//
//            val dappWellKnownProperties: Result<Pair<PublicKey, List<EngineDO.PushScope.Remote>>> = runCatching {
//                extractDidJson(dappUri).getOrThrow() to extractPushConfigUseCase(dappUri).getOrThrow()
//            }
//
//            dappWellKnownProperties.fold(
//                onSuccess = { (dappPublicKey, dappScope) -> createSubscription(dappPublicKey, dappScope) },
//                onFailure = { error -> return@supervisorScope onFailure(error) }
//            )
//        }

//    suspend fun approve(proposalRequestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
//        val proposalWithoutMetadata =
//            proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id $proposalRequestId"))
//        val dappMetadata: AppMetaData? = metadataStorageRepository.getByTopicAndType(proposalWithoutMetadata.proposalTopic, AppMetaDataType.PEER)
//        val proposalWithMetadata = with(proposalWithoutMetadata) {
//            EngineDO.PushProposal(requestId, proposalTopic, dappPublicKey, accountId, relayProtocolOptions, dappMetadata)
//        }
//
//        // Wallet sends push subscribe request to Push Server with subscriptionAuth
//        subscribeToDapp(
//            dappUri = proposalWithMetadata.dappMetadata?.url?.toUri() ?: String.Empty.toUri(),
//            account = proposalWithMetadata.accountId.value,
//            onSign = onSign,
//            onSuccess = { requestId, didJwt ->
//                CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(Dispatchers.IO) {
//                    enginePushSubscriptionNotifier.newlyRespondedRequestedSubscriptionId.asStateFlow()
//                        .filterNotNull()
//                        .filter { (id, _) -> id == requestId }
//                        .map { (_, activeSubscription) -> activeSubscription }
//                        .onEach { activeSubscription ->
//                            // Wallet generates key pair Z
//                            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
//                            val symKey = crypto.getSymmetricKey(activeSubscription.pushTopic.value.lowercase())
//                            val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)
//
//                            // Wallet responds with type 1 envelope on response topic to Dapp with subscriptionAuth and subscription symmetric key
//                            jsonRpcInteractor.respondWithParams(
//                                proposalWithMetadata.requestId,
//                                Topic(sha256(proposalWithMetadata.dappPublicKey.keyAsBytes)),
//                                clientParams = params,
//                                irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
//                                envelopeType = EnvelopeType.ONE,
//                                participants = Participants(
//                                    senderPublicKey = selfPublicKey,
//                                    receiverPublicKey = proposalWithMetadata.dappPublicKey
//                                )
//                            ) { error ->
//                                return@respondWithParams onFailure(error)
//                            }
//
//                            onSuccess()
//                        }.launchIn(this)
//                }
//            },
//            onFailure = {
//                onFailure(it)
//            }
//        )
//    }

//    suspend fun reject(proposalRequestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
//        try {
//            val proposalWithoutMetadata =
//                proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id $proposalRequestId"))
//
//            val responseTopic = Topic(sha256(proposalWithoutMetadata.dappPublicKey.keyAsBytes))
//
//            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
//
//            jsonRpcInteractor.respondWithError(proposalWithoutMetadata.requestId, responseTopic, PeerError.Rejected.UserRejected(reason), irnParams) { error ->
//                return@respondWithError onFailure(error)
//            }
//
//            onSuccess()
//        } catch (e: Exception) {
//            onFailure(e)
//        }
//    }

//    suspend fun update(pushTopic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
//        val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(pushTopic)
//            ?: return@supervisorScope onFailure(Exception("No subscription found for topic $pushTopic"))
//        val metadata: AppMetaData? = metadataStorageRepository.getByTopicAndType(subscription.pushTopic, AppMetaDataType.PEER)
//        val didJwt = registerIdentityAndReturnDidJwt(subscription.account, metadata?.url ?: String.Empty, scopes, { null }, onFailure).getOrElse { error ->
//            return@supervisorScope onFailure(error)
//        }
//
//        val updateParams = PushParams.UpdateParams(didJwt.value)
//        val request = PushRpc.PushUpdate(params = updateParams)
//        val irnParams = IrnParams(Tags.PUSH_UPDATE, Ttl(DAY_IN_SECONDS))
//        jsonRpcInteractor.publishJsonRpcRequest(Topic(pushTopic), irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
//    }

//    private suspend fun registerIdentityAndReturnDidJwt(
//        account: AccountId,
//        metadataUrl: String,
//        scopes: List<String>,
//        onSign: (String) -> Cacao.Signature?,
//        onFailure: (Throwable) -> Unit,
//    ): Result<DidJwt> = supervisorScope {
//        withContext(Dispatchers.IO) {
//            identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
//                onFailure(it)
//                this.cancel()
//            }
//        }
//
//        val joinedScope = scopes.joinToString(" ")
//        val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(account)
//
//        return@supervisorScope encodeDidJwt(
//            identityPrivateKey,
//            EncodePushAuthDidJwtPayloadUseCase(metadataUrl, account, joinedScope),
//            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
//        )
//    }

//    suspend fun deleteSubscription(pushTopic: String, onFailure: (Throwable) -> Unit) = supervisorScope {
//        val deleteParams = PushParams.DeleteParams(6000, "User Disconnected")
//        val request = PushRpc.PushDelete(id = generateId(), params = deleteParams)
//        val irnParams = IrnParams(Tags.PUSH_DELETE, Ttl(DAY_IN_SECONDS))
//
//        val activeSubscription: EngineDO.Subscription.Active =
//            subscriptionRepository.getActiveSubscriptionByPushTopic(pushTopic) ?: return@supervisorScope onFailure(IllegalStateException("Subscription does not exists for $pushTopic"))
//
//        subscriptionRepository.deleteSubscriptionByPushTopic(pushTopic)
//        messagesRepository.deleteMessagesByTopic(pushTopic)
//
//        jsonRpcInteractor.unsubscribe(Topic(pushTopic))
//        jsonRpcInteractor.publishJsonRpcRequest(Topic(pushTopic), irnParams, request,
//            onSuccess = {
//                CoreClient.Echo.unregister({
//                    logger.log("Delete sent successfully")
//                }, {
//                    onFailure(it)
//                })
//            },
//            onFailure = {
//                onFailure(it)
//            }
//        )
//
//        deleteSubscriptionToPushSubscriptionStoreUseCase(activeSubscription.account, activeSubscription.pushTopic, onSuccess = {}, onError = {})
//    }

//    suspend fun deleteMessage(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
//        try {
//            messagesRepository.deleteMessage(requestId)
//            onSuccess()
//        } catch (e: Exception) {
//            onFailure(e)
//        }
//    }

//    suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.PushMessage) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
//        try {
//            val decryptedMessageString = codec.decrypt(Topic(topic), message)
//            // How to look in JsonRpcHistory for dupes without Rpc ID
//            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return@supervisorScope onFailure(IllegalArgumentException("Unable to deserialize message"))
//            val pushMessage = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)
//            val pushMessageEngineDO = PushParams.MessageParams::class.safeCast(pushMessage)?.toEngineDO() ?: return@supervisorScope onFailure(IllegalArgumentException("Unable to deserialize message"))
//
//            onSuccess(pushMessageEngineDO)
//        } catch (e: Exception) {
//            onFailure(e)
//        }
//    }

//    suspend fun enableSync(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
//        setupSyncInPushUseCase(AccountId(account), onSign, onSuccess = {
//            scope.launch { getMessagesFromHistoryUseCase(AccountId(account), onSuccess, onFailure) }
//        }, onFailure)
//    }

    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.Subscription.Active> =
        subscriptionRepository.getAllActiveSubscriptions()
            .map { subscription ->
                val metadata = metadataStorageRepository.getByTopicAndType(subscription.pushTopic, AppMetaDataType.PEER)
                subscription.copy(dappMetaData = metadata)
            }
            .associateBy { subscription -> subscription.pushTopic.value }

    suspend fun getListOfMessages(topic: String): Map<Long, EngineDO.PushRecord> = supervisorScope {
        messagesRepository.getMessagesByTopic(topic).map { messageRecord ->
            EngineDO.PushRecord(
                id = messageRecord.id,
                topic = messageRecord.topic,
                publishedAt = messageRecord.publishedAt,
                message = EngineDO.PushMessage(
                    title = messageRecord.message.title,
                    body = messageRecord.message.body,
                    icon = messageRecord.message.icon,
                    url = messageRecord.message.url,
                    type = messageRecord.message.type,
                )
            )
        }.associateBy { pushRecord ->
            pushRecord.id
        }
    }

    private suspend fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PushParams.ProposeParams -> onPushPropose(request, requestParams)
                    is PushParams.MessageParams -> onPushMessage(request, requestParams)
                    is PushParams.DeleteParams -> onPushDelete(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { response -> response.params is PushParams }
            .onEach { response ->
                when (val responseParams = response.params) {
                    is PushParams.DeleteParams -> onPushDeleteResponse()
                    is PushParams.SubscribeParams -> onPushSubscribeResponse(response)
                    is PushParams.UpdateParams -> onPushUpdateResponse(response, responseParams)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(exception) }
            .launchIn(scope)

    private fun collectSyncUpdateEvents(): Job = syncClient.onSyncUpdateEvents
        .onEach { event -> onSyncUpdateEventUseCase(event) }
        .launchIn(scope)

    // Wallet receives push proposal with public key X on pairing topic
    private suspend fun onPushPropose(request: WCRequest, params: PushParams.ProposeParams) = supervisorScope {
        try {
            metadataStorageRepository.insertOrAbortMetadata(
                request.topic,
                params.metaData,
                AppMetaDataType.PEER
            )
        } catch (e: Exception) {
            logger.error("Cannot insert metadata: ${e.message}")
        }

        try {
            proposalStorageRepository.insertProposal(
                requestId = request.id,
                proposalTopic = request.topic.value,
                dappPublicKeyAsHex = params.publicKey,
                accountId = params.account,
            )

            _engineEvent.emit(
                EngineDO.PushProposal(
                    request.id,
                    Topic(request.topic.value),
                    PublicKey(params.publicKey),
                    AccountId(params.account),
                    RelayProtocolOptions(),
                    params.metaData
                )
            )
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                IrnParams(Tags.PUSH_PROPOSE_RESPONSE, Ttl(DAY_IN_SECONDS))
            )

            _engineEvent.emit(SDKError(e))
        }
    }

    private suspend fun onPushMessage(request: WCRequest, params: PushParams.MessageParams) = supervisorScope {
        val irnParams = IrnParams(Tags.PUSH_MESSAGE_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            // TODO: refactor to use the RPC published at value 
            val currentTime = request.id
            messagesRepository.insertMessage(request.id, request.topic.value, currentTime, params.title, params.body, params.icon, params.url, params.type)
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
        logger.error("onPushDelete: $request")
        val irnParams = IrnParams(Tags.PUSH_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))

        val result = try {
            val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(request.topic.value)

            if (subscription == null) {
                SDKError(IllegalStateException("Cannot find subscription for topic: ${request.topic}"))
            } else {
                jsonRpcInteractor.respondWithSuccess(request, irnParams)
                jsonRpcInteractor.unsubscribe(subscription.pushTopic)
                subscriptionRepository.deleteSubscriptionByPushTopic(subscription.pushTopic.value)

                EngineDO.PushDelete(request.topic.value)
            }
        } catch (e: Exception) {
            SDKError(e)
        }

        _engineEvent.emit(result)
    }

    private fun onPushDeleteResponse() {
        // TODO: Review if we need this
    }

    private suspend fun onPushSubscribeResponse(wcResponse: WCResponse) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val requestedSubscription: EngineDO.Subscription.Requested = subscriptionRepository.getRequestedSubscriptionByRequestId(response.id)
                        ?: return@supervisorScope _engineEvent.emit(SDKError(NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")))

                    // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val dappGeneratedPublicKey = PublicKey((((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String))
                    val selfPublicKey: PublicKey = crypto.getSelfPublicFromKeyAgreement(requestedSubscription.responseTopic)
                    val pushTopic: Topic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappGeneratedPublicKey)
                    val updatedExpiry: Expiry = calcExpiry()
                    val relayProtocolOptions = RelayProtocolOptions()

                    runCatching {
                        with(requestedSubscription) {
                            subscriptionRepository.insertOrAbortActiveSubscription(
                                account.value,
                                updatedExpiry.seconds,
                                relayProtocolOptions.protocol,
                                relayProtocolOptions.data,
                                mapOfScope.toDb(),
                                dappGeneratedPublicKey.keyAsHex,
                                pushTopic.value,
                                requestedSubscription.requestId
                            )
                        }
                    }.mapCatching {
                        metadataStorageRepository.updateOrAbortMetaDataTopic(requestedSubscription.responseTopic, pushTopic)
                    }.fold(onSuccess = {
                        val activeSubscription = with(requestedSubscription) {
                            val dappMetaData: AppMetaData? = metadataStorageRepository.getByTopicAndType(pushTopic, AppMetaDataType.PEER)

                            EngineDO.Subscription.Active(account, mapOfScope, expiry, dappGeneratedPublicKey, pushTopic, dappMetaData, requestedSubscription.requestId)
                        }

                        jsonRpcInteractor.subscribe(pushTopic) { error ->
                            launch {
                                _engineEvent.emit(SDKError(error))
                                cancel()
                            }
                        }

                        jsonRpcInteractor.unsubscribe(requestedSubscription.responseTopic) { error ->
                            launch {
                                _engineEvent.emit(SDKError(error))
                                cancel()
                            }
                        }

                        val symKey = crypto.getSymmetricKey(pushTopic.value.lowercase())
                        setSubscriptionWithSymmetricKeyToPushSubscriptionStoreUseCase(activeSubscription, symKey, { logger.log("Synced Subscriptions") }, { error -> logger.error(error) })

                        _engineEvent.emit(activeSubscription)
                        enginePushSubscriptionNotifier.newlyRespondedRequestedSubscriptionId.updateAndGet { requestedSubscription.requestId to activeSubscription }
                    }, onFailure = {
                        logger.error(it)
                        return@supervisorScope _engineEvent.emit(SDKError(NotFoundException("Subscription already exists for topic: ${wcResponse.topic.value}")))
                    })
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _engineEvent.emit(EngineDO.Subscription.Error(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _engineEvent.emit(SDKError(exception))
        }
    }

    private suspend fun onPushUpdateResponse(wcResponse: WCResponse, updateParams: PushParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(wcResponse.topic.value)
                        ?: throw NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val pushUpdateJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _engineEvent.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = pushUpdateJwtClaim.scope.split(" ")
                    val updateScopeMap: Map<String, EngineDO.PushScope.Cached> = subscription.mapOfScope.entries.associate { (scopeName, scopeDescIsSelected) ->
                        val (desc, _) = scopeDescIsSelected
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to EngineDO.PushScope.Cached(scopeName, desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionRepository.updateSubscriptionScopeAndJwtByPushTopic(
                        subscription.pushTopic.value,
                        updateScopeMap.toDb(),
                        newExpiry.seconds
                    )

                    with(subscription) { EngineDO.PushUpdate.Result(account, mapOfScope, expiry, dappGeneratedPublicKey, pushTopic, dappMetaData, relay) }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    EngineDO.PushUpdate.Error(wcResponse.response.id, response.error.message)
                }
            }
        } catch (exception: Exception) {
            SDKError(exception)
        }
        _engineEvent.emit(resultEvent)
    }

    private suspend fun resubscribeToSubscriptions() {
        val subscriptionTopics = getListOfActiveSubscriptions().keys.toList()
        jsonRpcInteractor.batchSubscribe(subscriptionTopics) { error -> scope.launch { _engineEvent.emit(SDKError(error)) } }
    }

    private companion object {
        const val DID_JSON = ".well-known/did.json"
    }
}