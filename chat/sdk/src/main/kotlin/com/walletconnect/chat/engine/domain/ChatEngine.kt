@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.Cacao.Payload.Companion.CURRENT_VERSION
import com.walletconnect.android.internal.common.cacao.Cacao.Payload.Companion.ISO_8601_PATTERN
import com.walletconnect.android.internal.common.cacao.CacaoType
import com.walletconnect.android.internal.common.cacao.toCAIP122Message
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.*
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.chat.authentication.jwt.InviteKeyJwtRepository
import com.walletconnect.chat.common.exceptions.InvalidAccountIdException
import com.walletconnect.chat.common.exceptions.PeerError
import com.walletconnect.chat.common.exceptions.UnableToExtractDomainException
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveInviteUseCase
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.json_rpc.JsonRpcMethod
import com.walletconnect.chat.storage.ChatStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

internal class ChatEngine(
    private val keyserverUrl: String,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val registerInviteUseCase: RegisterInviteUseCase,
    private val resolveInviteUseCase: ResolveInviteUseCase,
    private val inviteKeyJwtRepository: InviteKeyJwtRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val chatStorage: ChatStorageRepository,
    private val pairingHandler: PairingControllerInterface,
    private val threadsRepository: ThreadsStorageRepository,
    private val logger: Logger,
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()
    private val inviteRequestMap: MutableMap<Long, WCRequest> = mutableMapOf()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_CHAT_INVITE,
            JsonRpcMethod.WC_CHAT_MESSAGE,
            JsonRpcMethod.WC_CHAT_LEAVE,
            JsonRpcMethod.WC_CHAT_PING
        )
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _events.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) {
                        trySubscribeToInviteTopic()
                    }
                }
                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }
                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectPeerResponses()
                }
                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    private fun generateIdAuth(inviteKey: PublicKey, accountId: AccountId): String {
        val tag = accountId.getIdentityTag()
        val identityPublicKey = keyManagementRepository.getPublicKey(tag)
        val identityKeyPair = keyManagementRepository.getKeyPair(identityPublicKey)

        return inviteKeyJwtRepository.generateInviteKeyJWT(inviteKey.keyAsHex, identityKeyPair, keyserverUrl, accountId)
    }

    internal fun registerInvite(
        accountId: AccountId,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        private: Boolean,
    ) {
        fun onSuccess(publicKey: PublicKey) {
            val topic = keyManagementRepository.getTopicFromKey(publicKey)
            keyManagementRepository.setKey(publicKey, SELF_INVITE_PUBLIC_KEY_CONTEXT)
            keyManagementRepository.setKey(publicKey, "$SELF_PARTICIPANT_CONTEXT${topic.value}")
            onSuccess(publicKey.keyAsHex)
        }

        if (accountId.isValid()) {
            try {
                val storedPublicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
                onSuccess(storedPublicKey.keyAsHex)
            } catch (e: MissingKeyException) {
                val publicKey = keyManagementRepository.generateAndStoreX25519KeyPair()

                val idAuth = generateIdAuth(publicKey, accountId)

                if (!private) {
                    scope.launch {
                        supervisorScope {
                            registerInviteUseCase(idAuth).fold(
                                onSuccess = { onSuccess(publicKey) },
                                onFailure = { error -> onFailure(error) }
                            )
                        }
                    }
                } else {
                    onSuccess(publicKey)
                }
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun registerIdentity(
        accountId: AccountId,
        onSign: (String) -> Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        private: Boolean,
    ) {
        fun onSuccess(publicKey: PublicKey) {
            keyManagementRepository.setKey(publicKey, accountId.getIdentityTag())
            onSuccess(publicKey.keyAsHex)
        }

        if (accountId.isValid()) {
            try {
                val storedPublicKey = keyManagementRepository.getPublicKey(accountId.getIdentityTag())
                onSuccess(storedPublicKey.keyAsHex)
            } catch (e: MissingKeyException) {
                val identityKey = keyManagementRepository.generateAndStoreEd25519KeyPair()
                val didKey = encodeDidKey(identityKey.keyAsBytes)

                val domain = keyserverUrl.toDomain().getOrElse {
                    onFailure(UnableToExtractDomainException("Unable to extract domain from: $keyserverUrl"))
                    return@registerIdentity
                }

                val payload = Cacao.Payload(
                    iss = encodeDidPkh(accountId),
                    domain = domain,
                    aud = keyserverUrl, version = CURRENT_VERSION,
                    nonce = randomBytes(32).toString(), iat = SimpleDateFormat(ISO_8601_PATTERN, Locale.getDefault()).format(Calendar.getInstance().time),
                    nbf = null, exp = null, statement = null, requestId = null, resources = listOf(didKey)
                )

                val message = payload.toCAIP122Message()
                val signature = onSign(message)

                val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)

                if (!private) {
                    scope.launch {
                        supervisorScope {
                            registerIdentityUseCase(cacao).fold(
                                onSuccess = { onSuccess(identityKey) },
                                onFailure = { error -> onFailure(error) }
                            )
                        }
                    }
                } else {
                    onSuccess(identityKey)
                }
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun resolveAccount(accountId: AccountId, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (accountId.isValid()) {
            scope.launch {
                supervisorScope {
                    resolveInviteUseCase(accountId).fold(
                        onSuccess = { response -> onSuccess(response.inviteKey) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun invite(peerAccount: AccountId, invite: EngineDO.Invite, onFailure: (Throwable) -> Unit) {
        addContact(AccountIdWithPublicKey(peerAccount, PublicKey(invite.publicKey))) { error ->
            logger.error("Error while adding new account: $error")
            return@addContact onFailure(error)
        }
        val senderPublicKey = try {
            keyManagementRepository.generateAndStoreX25519KeyPair()
        } catch (e: Exception) {
            return onFailure(e)
        }

        try {
            val peerPublicKey = PublicKey(invite.publicKey)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(senderPublicKey, peerPublicKey)
            val inviteTopic = keyManagementRepository.getTopicFromKey(peerPublicKey)
            keyManagementRepository.setKeyAgreement(inviteTopic, senderPublicKey, peerPublicKey)

            val participants = Participants(senderPublicKey = senderPublicKey, receiverPublicKey = peerPublicKey)
            val inviteParams = ChatParams.InviteParams(invite.message, invite.accountId.value, senderPublicKey.keyAsHex, invite.signature)

            val payload = ChatRpc.ChatInvite(id = generateId(), params = inviteParams)
            val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)

            keyManagementRepository.setKey(symmetricKey, acceptTopic.value)
            threadsRepository.insertThread(acceptTopic.value, invite.accountId.value, peerAccount.value)
            jsonRpcInteractor.subscribe(acceptTopic) { error -> return@subscribe onFailure(error) }

            val irnParams = IrnParams(Tags.CHAT_INVITE, Ttl(DAY_IN_SECONDS), true)
            jsonRpcInteractor.publishJsonRpcRequest(inviteTopic, irnParams, payload, EnvelopeType.ONE, participants,
                { logger.log("Chat invite sent successfully") },
                { throwable ->
                    logger.log("Chat invite error: $throwable")
                    jsonRpcInteractor.unsubscribe(acceptTopic)
                    onFailure(throwable)
                }
            )
        } catch (error: Exception) {
            keyManagementRepository.removeKeys(senderPublicKey.keyAsHex)
            onFailure(error)
        }
    }

    internal fun addContact(accountIdWithPublicKeyVO: AccountIdWithPublicKey, onFailure: (Throwable) -> Unit) = try {
        if (chatStorage.doesContactNotExists(accountIdWithPublicKeyVO.accountId)) {
            chatStorage.createContact(EngineDO.Contact(accountIdWithPublicKeyVO, accountIdWithPublicKeyVO.accountId.value))
        } else {
            chatStorage.updateContact(
                accountIdWithPublicKeyVO.accountId,
                accountIdWithPublicKeyVO.publicKey,
                accountIdWithPublicKeyVO.accountId.value
            )
        }
    } catch (error: Exception) {
        onFailure(error)
    }

    internal fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) =
        try {
            val request = inviteRequestMap[inviteId] ?: throw GenericException("No request for inviteId")
            val senderPublicKey = PublicKey((request.params as ChatParams.InviteParams).publicKey)
            inviteRequestMap.remove(inviteId)

            //TODO: Missing accountId in accept
            val invitePublicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(invitePublicKey, senderPublicKey)
            val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, acceptTopic.value)

            val publicKey = keyManagementRepository.generateAndStoreX25519KeyPair()
            val acceptanceParams = CoreChatParams.AcceptanceParams(publicKey.keyAsHex)
            val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithParams(request.copy(topic = acceptTopic), acceptanceParams, irnParams, EnvelopeType.ZERO) { error ->
                onFailure(error)
                return@respondWithParams
            }

            val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, senderPublicKey)
            val threadTopic = keyManagementRepository.getTopicFromKey(threadSymmetricKey)
            keyManagementRepository.setKey(threadSymmetricKey, threadTopic.value)
            jsonRpcInteractor.subscribe(threadTopic) { error -> return@subscribe onFailure(error) }
            onSuccess(threadTopic.value)
        } catch (error: Exception) {
            onFailure(error)
        }

    internal fun reject(inviteId: Long, onFailure: (Throwable) -> Unit) {
        try {
            val request = inviteRequestMap[inviteId] ?: throw GenericException("No request for inviteId")
            val senderPublicKey = PublicKey((request.params as ChatParams.InviteParams).publicKey)
            inviteRequestMap.remove(inviteId)

            //TODO: Missing accountId in reject
            val invitePublicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(invitePublicKey, senderPublicKey)
            val rejectTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, rejectTopic.value)

            val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(DAY_IN_SECONDS))
            jsonRpcInteractor.respondWithError(
                request.copy(topic = rejectTopic),
                PeerError.UserRejectedInvitation("Invitation rejected by a user"),
                irnParams
            ) { throwable -> onFailure(throwable) }
        } catch (e: MissingKeyException) {
            return onFailure(e)
        }
    }

    internal fun message(topic: String, sendMessage: EngineDO.SendMessage, onFailure: (Throwable) -> Unit) {
        //todo resolve AUTHOR_ACCOUNT from thread storage by topic
        val messageParams = ChatParams.MessageParams(sendMessage.message, sendMessage.author.value, System.currentTimeMillis(), sendMessage.media)
        val payload = ChatRpc.ChatMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParams(Tags.CHAT_MESSAGE, Ttl(DAY_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload,
            onSuccess = { logger.log("Chat message sent successfully") },
            onFailure = { throwable ->
                logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        val payload = ChatRpc.ChatLeave(id = generateId(), params = ChatParams.LeaveParams())
        val irnParams = IrnParams(Tags.CHAT_LEAVE, Ttl(DAY_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload, EnvelopeType.ZERO,
            onSuccess = { logger.log("Chat leave sent successfully") },
            onFailure = { throwable ->
                logger.log("Chat leave error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        val pingPayload = ChatRpc.ChatPing(id = generateId(), params = ChatParams.PingParams())
        val irnParams = IrnParams(Tags.CHAT_PING, Ttl(THIRTY_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
            onSuccess = { pingSuccess(pingPayload, onSuccess, topic, onFailure) },
            onFailure = { error ->
                logger.log("Ping sent error: $error")
                onFailure(error)
            })
    }

    internal fun getThreadsByAccount(accountId: String): Map<String, Thread> {
        return threadsRepository.getThreadsForSelfAccount(accountId).associateBy { thread -> thread.topic }
    }

    private fun pingSuccess(
        pingPayload: ChatRpc.ChatPing,
        onSuccess: (String) -> Unit,
        topic: String,
        onFailure: (Throwable) -> Unit,
    ) {
        logger.log("Ping sent successfully")
        scope.launch {
            try {
                withTimeout(THIRTY_SECONDS_TIMEOUT) {
                    collectResponse(pingPayload.id) { result ->
                        cancel()
                        result.fold(
                            onSuccess = {
                                logger.log("Ping peer response success")
                                onSuccess(topic)
                            },
                            onFailure = { error ->
                                logger.log("Ping peer response error: $error")
                                onFailure(error)
                            })
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onFailure(e)
            }
        }
    }

    private fun onInviteRequest(wcRequest: WCRequest, params: ChatParams.InviteParams) {
        inviteRequestMap[wcRequest.id] = wcRequest
        scope.launch {
            val invite = EngineDO.Invite(AccountId(params.account), params.message, params.publicKey, params.signature)
            _events.emit(EngineDO.Events.OnInvite(wcRequest.id, invite))
        }
        //TODO: Add adding invites to storage. For Alpha we will use only emitted event.
    }

    private fun onMessage(wcRequest: WCRequest, params: ChatParams.MessageParams) {
        scope.launch {
            val message = EngineDO.Message(params.message, AccountId(params.authorAccount), params.timestamp, params.media)
            _events.emit(EngineDO.Events.OnMessage(wcRequest.topic.value, message))
        }
        //TODO: Add adding messages to storage. For Alpha we will use only emitted event.
    }

    private fun onLeft(request: WCRequest) {
        threadsRepository.deleteThreadByTopic(request.topic.value)

        scope.launch {
            _events.emit(EngineDO.Events.OnLeft(request.topic.value))
        }
    }

    private fun onPong(request: WCRequest) {
        jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS)))
    }

    private fun onInviteResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                logger.log("Chat invite was rejected")
                scope.launch { _events.emit(EngineDO.Events.OnReject(wcResponse.topic.value)) }
            }
            is JsonRpcResponse.JsonRpcResult -> onInviteAccepted(response, wcResponse)
        }
    }

    private fun onInviteAccepted(response: JsonRpcResponse.JsonRpcResult, wcResponse: WCResponse) {
        logger.log("Chat invite was accepted")
        val acceptParams = response.result as CoreChatParams.AcceptanceParams
        val pubKeyZ = PublicKey(acceptParams.publicKey)

        try {
            val selfPubKey: PublicKey = keyManagementRepository.getSelfPublicFromKeyAgreement(wcResponse.topic)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, pubKeyZ)
            val threadTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, threadTopic.value)
            jsonRpcInteractor.subscribe(threadTopic) { error ->
                scope.launch {
                    _events.emit(SDKError(InternalError(error)))
                }
                return@subscribe
            }

            threadsRepository.insertThread(threadTopic.value, TODO("How to get the self account"), TODO("How to get the peer account"))
            scope.launch { _events.emit(EngineDO.Events.OnJoined(threadTopic.value)) }
        } catch (e: Exception) {
            scope.launch { _events.emit(SDKError(InternalError(e))) }
            return
        }
    }

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit = {}) {
        jsonRpcInteractor.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponse.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponse.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is ChatParams }
            .onEach { request ->
                when (val params = request.params) {
                    is ChatParams.InviteParams -> onInviteRequest(request, params)
                    is ChatParams.MessageParams -> onMessage(request, params)
                    is ChatParams.LeaveParams -> onLeft(request)
                    is ChatParams.PingParams -> onPong(request)
                }
            }.launchIn(scope)

    private fun collectPeerResponses(): Job =
        scope.launch {
            jsonRpcInteractor.peerResponse.collect { response ->
                when (response.params) {
                    is ChatParams.InviteParams -> onInviteResponse(response)
                }
            }
        }

    private fun trySubscribeToInviteTopic() {
        try {
            val publicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
            val topic = keyManagementRepository.getTopicFromKey(publicKey)
            jsonRpcInteractor.subscribe(topic) { error ->
                scope.launch { _events.emit(SDKError(InternalError(error))) }
            }
            logger.log("Listening for invite on: $topic, pubKey X:$publicKey")
        } catch (error: Exception) {
            scope.launch { _events.emit(SDKError(InternalError(error))) }
        }
    }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _events.emit(SDKError(exception)) }
            .launchIn(scope)

    //TODO: Stolen from BaseJwtRepository - should be refactored
    private fun encodeDidKey(publicKey: ByteArray): String {
        val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

        return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
    }

    private fun encodeDidPkh(accountId: AccountId): String {
        return listOf(DID_PREFIX, DID_METHOD_PKH, accountId.value).joinToString(DID_DELIMITER)
    }

    private fun AccountId.getIdentityTag(): String = "$SELF_IDENTITY_PUBLIC_KEY_CONTEXT${this.value}"


    private fun String.toDomain(): Result<String> = runCatching {
        val uri = URI(this)
        val domain: String = uri.host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L

        //TODO: Stolen from BaseJwtRepository - should be refactored
        private const val MULTICODEC_ED25519_HEADER = "K36"
        private const val DID_DELIMITER = ":"
        private const val DID_PREFIX = "did"
        private const val DID_METHOD_KEY = "key"
        private const val DID_METHOD_PKH = "pkh"
    }
}