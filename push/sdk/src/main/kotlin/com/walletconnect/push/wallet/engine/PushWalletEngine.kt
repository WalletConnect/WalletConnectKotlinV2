@file:JvmSynthetic

package com.walletconnect.push.wallet.engine

import android.content.res.Resources.NotFoundException
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
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
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.wallet.data.MessagesRepository
import com.walletconnect.push.wallet.engine.domain.ApproveUseCase
import com.walletconnect.push.wallet.engine.domain.ApproveUseCaseInterface
import com.walletconnect.push.wallet.engine.domain.SubscribeToDappUseCase
import com.walletconnect.push.wallet.engine.domain.SubscribeToDappUseCaseInterface
import com.walletconnect.util.generateId
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.reflect.full.safeCast

internal class PushWalletEngine(
    private val keyserverUrl: String,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingHandler: PairingControllerInterface,
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val messagesRepository: MessagesRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val serializer: JsonRpcSerializer,
    private val logger: Logger,
    private val subscriptToDappUseCase: SubscribeToDappUseCase,
    private val approveUseCase: ApproveUseCase,
) : SubscribeToDappUseCaseInterface by subscriptToDappUseCase,
    ApproveUseCaseInterface by approveUseCase {
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
                    is PushParams.MessageParams -> onPushMessage(request, requestParams)
                    is PushParams.DeleteParams -> onPushDelete(request)
                }
            }.launchIn(scope)

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse.onEach { response ->
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

    private suspend fun onPushRequest(request: WCRequest, params: PushParams.RequestParams) = supervisorScope {
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            subscriptionStorageRepository.insertSubscription(
                requestId = request.id,
                keyAgreementTopic = "",
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

            val newSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(request.id)

            _engineEvent.emit(
                EngineDO.PushRequest(
                    newSubscription.requestId,
                    newSubscription.responseTopic.value,
                    newSubscription.account.value,
                    newSubscription.relay,
                    newSubscription.metadata
                )
            )
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                irnParams
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
                    val dappPublicKey =
                        PublicKey((((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String)) // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
                    val updatedExpiry = calcExpiry()
                    subscriptionStorageRepository.updateSubscriptionToResponded(subscription.responseTopic.value, pushTopic.value, dappPublicKey.keyAsHex, updatedExpiry)

                    _engineEvent.emit(
                        subscription.copy(
                            subscriptionTopic = pushTopic,
                            peerPublicKey = PublicKey(dappPublicKey.keyAsHex),
                            expiry = Expiry(updatedExpiry)
                        )
                    )
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
                    val updateScopeMap: Map<String, Pair<String, Boolean>> = subscription.scope.entries.associate { (scopeName, descAndValue) ->
                        val (desc, _) = descAndValue
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to Pair(desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionStorageRepository.updateSubscriptionScopeAndJwt(wcResponse.topic.value, updateScopeMap, updateParams.subscriptionAuth, newExpiry)

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