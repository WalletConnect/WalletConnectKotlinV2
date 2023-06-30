@file:JvmSynthetic

package com.walletconnect.push.wallet.engine

import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Listing
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Redirect
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
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.jwt.EncodePushAuthDidJwtPayloadUseCase
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.domain.ExtractPushConfigUseCase
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.wallet.data.MessagesRepository
import com.walletconnect.push.wallet.data.wellknown.did.DidJsonDTO
import com.walletconnect.push.wallet.engine.domain.EnginePushSubscriptionNotifier
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.generateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.reflect.full.safeCast

internal class PushWalletEngine(
    private val keyserverUrl: String,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val messagesRepository: MessagesRepository,
    private val enginePushSubscriptionNotifier: EnginePushSubscriptionNotifier,
    private val identitiesInteractor: IdentitiesInteractor,
    private val serializer: JsonRpcSerializer,
    private val explorerRepository: ExplorerRepository,
    private val extractPushConfigUseCase: ExtractPushConfigUseCase,
    private val codec: Codec,
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
            JsonRpcMethod.WC_PUSH_MESSAGE,
            JsonRpcMethod.WC_PUSH_DELETE,
            JsonRpcMethod.WC_PUSH_PROPOSE,
            JsonRpcMethod.WC_PUSH_SUBSCRIBE,
            JsonRpcMethod.WC_PUSH_UPDATE,
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

    suspend fun subscribeToDapp(dappUri: Uri, account: String, onSign: (String) -> Cacao.Signature?, onSuccess: (Long, DidJwt) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        suspend fun createSubscription(dappPublicKey: PublicKey, dappScopes: List<EngineDO.PushScope.Remote>) {
            val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))
            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
            val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
            val dappMetaData: AppMetaData = withContext(Dispatchers.IO) {
                // Fetch dapp metadata from explorer api
                val listOfDappHomepages = runCatching {
                    explorerRepository.getAllDapps().listings.associateBy { listing -> listing.homepage.host }
                }.getOrElse { error ->
                    return@withContext Result.failure(error)
                }

                // Find dapp metadata for dapp uri
                val (dappHomepageHost: String?, dappListing: Listing) = listOfDappHomepages.entries.filter { (dappHomepageHost, dappListing) ->
                    dappHomepageHost != null && dappListing.description != null
                }.firstOrNull { (dappHomepageHost, _) ->
                    dappHomepageHost != null && dappHomepageHost.contains(dappUri.host!!)
                } ?: return@withContext Result.failure<AppMetaData>(IllegalArgumentException("Unable to find dapp listing for $dappUri"))

                // Return dapp metadata
                return@withContext Result.success(
                    AppMetaData(
                        name = dappListing.name,
                        description = dappListing.description!!,
                        icons = listOf(dappListing.imageUrl.sm, dappListing.imageUrl.md, dappListing.imageUrl.lg),
                        url = dappHomepageHost!!,
                        redirect = Redirect(dappListing.app.android)
                    )
                )
            }.getOrElse {
                return onFailure(it)
            }

            val didJwt = registerIdentityAndReturnDidJwt(AccountId(account), dappUri.toString(), dappScopes.map { it.name }, onSign, onFailure).getOrElse { error ->
                return onFailure(error)
            }
            val params = PushParams.SubscribeParams(didJwt.value)
            val request = PushRpc.PushSubscribe(params = params)
            val irnParams = IrnParams(Tags.PUSH_SUBSCRIBE, Ttl(DAY_IN_SECONDS))

            coroutineScope {
                launch {
                    subscriptionStorageRepository.insertSubscription(
                        requestId = request.id,
                        keyAgreementTopic = responseTopic.value,
                        responseTopic = subscribeTopic.value,
                        peerPublicKeyAsHex = null,
                        subscriptionTopic = null,
                        account = account,
                        relayProtocol = null,
                        relayData = null,
                        name = dappMetaData.name,
                        description = dappMetaData.description,
                        url = dappMetaData.url,
                        icons = dappMetaData.icons,
                        native = dappMetaData.redirect?.native,
                        didJwt = didJwt.value,
                        mapOfScope = dappScopes.associate { scope -> scope.name to Pair(scope.description, true) },
                        expiry = calcExpiry()
                    )
                }
            }

            jsonRpcInteractor.subscribe(responseTopic) { error ->
                return@subscribe onFailure(error)
            }

            jsonRpcInteractor.publishJsonRpcRequest(
                topic = subscribeTopic,
                params = irnParams,
                payload = request,
                envelopeType = EnvelopeType.ONE,
                participants = Participants(selfPublicKey, dappPublicKey),
                onSuccess = {
                    onSuccess(request.id, didJwt)
                },
                onFailure = { error ->
                    onFailure(error)
                }
            )
        }

        suspend fun extractDidJson(dappUri: Uri): Result<PublicKey> = withContext(Dispatchers.IO) {
            val didJsonDappUri = dappUri.run {
                if (this.path?.contains(DID_JSON) == false) {
                    this.buildUpon().appendPath(DID_JSON).build()
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

        val dappWellKnownProperties: Result<Pair<PublicKey, List<EngineDO.PushScope.Remote>>> = runCatching {
            extractDidJson(dappUri).getOrThrow() to extractPushConfigUseCase(dappUri).getOrThrow()
        }

        dappWellKnownProperties.fold(
            onSuccess = { (dappPublicKey, dappScope) -> createSubscription(dappPublicKey, dappScope) },
            onFailure = { error -> return@supervisorScope onFailure(error) }
        )
    }

    suspend fun approve(proposalRequestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposal = proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id"))

        subscribeToDapp(
            dappUri = proposal.dappMetaData.url.toUri(),
            account = proposal.accountId.value,
            onSign = onSign,
            onSuccess = { subscriptionRequestId, didJwt ->
                CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(Dispatchers.IO) {
                    enginePushSubscriptionNotifier.newlyCreatedPushSubscription.asStateFlow()
                        .filter { subscription ->
                            subscription != null && subscription.requestId == subscriptionRequestId && subscription.subscriptionTopic != null
                        }
                        .filterNotNull()
                        .onEach { subscription ->
                            val responseTopic = Topic(sha256(proposal.dappPublicKey.keyAsBytes))
                            // Wallet generates key pair Z
                            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                            val symKey = crypto.getSymmetricKey(subscription.subscriptionTopic!!.value)
                            val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)

                            jsonRpcInteractor.respondWithParams(
                                proposal.requestId,
                                responseTopic,
                                clientParams = params,
                                irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
                                envelopeType = EnvelopeType.ONE,
                                participants = Participants(
                                    senderPublicKey = selfPublicKey,
                                    receiverPublicKey = proposal.dappPublicKey
                                )
                            ) { error ->
                                return@respondWithParams onFailure(error)
                            }

                            onSuccess()
                        }.launchIn(this)
                }
            },
            onFailure = {
                onFailure(it)
            }
        )
    }

    suspend fun reject(requestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithError(respondedSubscription.requestId, respondedSubscription.responseTopic, PeerError.Rejected.UserRejected(reason), irnParams) { error ->
                return@respondWithError onFailure(error)
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun update(topic: String, scopes: List<String>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { subscription -> subscription.subscriptionTopic?.value == topic }
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
    ): Result<DidJwt> = supervisorScope {
        withContext(Dispatchers.IO) {
            identitiesInteractor.registerIdentity(account, keyserverUrl, onSign).getOrElse {
                onFailure(it)
                this.cancel()
            }
        }

        val joinedScope = scopes.joinToString(" ")
        val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(account)

        return@supervisorScope encodeDidJwt(
            identityPrivateKey,
            EncodePushAuthDidJwtPayloadUseCase(metadataUrl, account, joinedScope),
            EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
        )
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

    suspend fun deleteMessage(requestId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            messagesRepository.deleteMessage(requestId)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.PushMessage) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            // How to look in JsonRpcHistory for dupes without Rpc ID
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return@supervisorScope onFailure(IllegalArgumentException("Unable to deserialize message"))
            val pushMessage = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)
            val pushMessageEngineDO = PushParams.MessageParams::class.safeCast(pushMessage)?.toEngineDO() ?: return@supervisorScope onFailure(IllegalArgumentException("Unable to deserialize message"))

            onSuccess(pushMessageEngineDO)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun getListOfActiveSubscriptions(): Map<String, EngineDO.PushSubscription> =
        subscriptionStorageRepository.getAllSubscriptions()
            .filter { subscription -> subscription.subscriptionTopic?.value.isNullOrBlank().not() }
            .associateBy { subscription -> subscription.subscriptionTopic!!.value }

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
                    is PushParams.RequestParams -> onPushRequest(request, requestParams)
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

    // Wallet receives push proposal with public key X on pairing topic
    private suspend fun onPushPropose(request: WCRequest, params: PushParams.ProposeParams) = supervisorScope {
        try {
            metadataStorageRepository.insertOrAbortMetadata(
                request.topic,
                params.metaData,
                AppMetaDataType.PEER
            )

            metadataStorageRepository.lastInsertedId()
        } catch (e: Exception) {
            metadataStorageRepository.getIdByTopicAndType(request.topic, AppMetaDataType.PEER)
        }

        try {
            proposalStorageRepository.insertProposal(
                requestId = request.id,
                proposalTopic = request.topic.value,
                dappPublicKeyAsHex = params.publicKey,
                dappMetaDataId = metadataId,
                accountId = params.account,
            )

            _engineEvent.emit(
                EngineDO.PushPropose(
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
            val currentTime = CURRENT_TIME_IN_SECONDS
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
                    val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { it.responseTopic.value == wcResponse.topic.value } ?: return@supervisorScope _engineEvent.emit(
                        SDKError(NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}"))
                    )
                    val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(subscription.keyAgreementTopic)
                    val dappPublicKey = PublicKey(
                        (((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String)
                    ) // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
                    val updatedExpiry = calcExpiry()
                    subscriptionStorageRepository.updateSubscriptionToResponded(subscription.responseTopic.value, pushTopic.value, dappPublicKey.keyAsHex, updatedExpiry)
                    val updatedSubscription = subscription.copy(
                        subscriptionTopic = pushTopic,
                        peerPublicKey = PublicKey(dappPublicKey.keyAsHex),
                        expiry = Expiry(updatedExpiry)
                    )

                    jsonRpcInteractor.subscribe(pushTopic) { error ->
                        launch {
                            _engineEvent.emit(SDKError(error))
                            cancel()
                        }
                    }

                    jsonRpcInteractor.unsubscribe(wcResponse.topic) { error ->
                        launch {
                            _engineEvent.emit(SDKError(error))
                            cancel()
                        }
                    }

                    _engineEvent.emit(updatedSubscription)
                    enginePushSubscriptionNotifier.newlyCreatedPushSubscription.updateAndGet { updatedSubscription }
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
                    val subscription = subscriptionStorageRepository.getAllSubscriptions().firstOrNull { it.subscriptionTopic?.value == wcResponse.topic.value }
                        ?: throw NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val pushUpdateJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _engineEvent.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = pushUpdateJwtClaim.scope.split(" ")
                    val updateScopeMap: Map<String, EngineDO.PushScope.Cached> = subscription.scope.entries.associate { (scopeName, scopeDescIsSelected) ->
                        val (desc, _) = scopeDescIsSelected
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to EngineDO.PushScope.Cached(scopeName, desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionStorageRepository.updateSubscriptionScopeAndJwt(
                        wcResponse.topic.value,
                        updateScopeMap.mapValues { (_, pushScope) -> pushScope.description to pushScope.isSelected },
                        updateParams.subscriptionAuth,
                        newExpiry
                    )

                    _engineEvent.emit(
                        EngineDO.PushUpdate(
                            requestId = subscription.requestId,
                            responseTopic = subscription.responseTopic.value,
                            peerPublicKeyAsHex = subscription.peerPublicKey?.keyAsHex,
                            subscriptionTopic = subscription.subscriptionTopic?.value,
                            account = subscription.account,
                            relay = subscription.relay,
                            metadata = subscription.metadata,
                            didJwt = subscription.didJwt,
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

    private companion object {
        const val DID_JSON = ".well-known/did.json"
        const val WC_PUSH_CONFIG_JSON = ".well-known/wc-push-config.json"
    }
}