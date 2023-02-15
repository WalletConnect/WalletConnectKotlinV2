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
import com.walletconnect.chat.common.exceptions.*
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.*
import com.walletconnect.chat.discovery.keyserver.domain.use_case.*
import com.walletconnect.chat.json_rpc.JsonRpcMethod
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.jwt.DidJwtRepository
import com.walletconnect.chat.jwt.use_case.*
import com.walletconnect.chat.storage.ChatStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.*
import com.walletconnect.util.generateId
import com.walletconnect.util.randomBytes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

internal class ChatEngine(
    private val keyserverUrl: String,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val unregisterIdentityUseCase: UnregisterIdentityUseCase,
    private val resolveIdentityUseCase: ResolveIdentityUseCase,
    private val registerInviteUseCase: RegisterInviteUseCase,
    private val unregisterInviteUseCase: UnregisterInviteUseCase,
    private val resolveInviteUseCase: ResolveInviteUseCase,
    private val didJwtRepository: DidJwtRepository,
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

    // todo: This should be persisted. As a part of multi-account storage task
    private val inviteRequestMap: MutableMap<Long, Pair<WCRequest, ChatDidJwtClaims.InviteProposal>> = mutableMapOf()
    private val inviteTopicsToInvitePublicKeys: MutableMap<Topic, Pair<AccountId, PublicKey>> = mutableMapOf() // todo refactor inviteTopics

    // TODO: Persist. As a part of multi-account storage task
    private val identities: MutableMap<String, Cacao> = mutableMapOf()

    // Remove after pairing refactor
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

    private fun getIdentityKeyPair(accountId: AccountId): Pair<PublicKey, PrivateKey> {
        val tag = accountId.getIdentityTag()
        val identityPublicKey = keyManagementRepository.getPublicKey(tag)
        return keyManagementRepository.getKeyPair(identityPublicKey)
    }

    internal fun goPublic(
        accountId: AccountId,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        fun onSuccess(publicKey: PublicKey) {
            val inviteTopic = keyManagementRepository.getTopicFromKey(publicKey)
            keyManagementRepository.setKey(publicKey, accountId.getInviteTag())
            keyManagementRepository.setKey(publicKey, "$SELF_PARTICIPANT_CONTEXT${inviteTopic.value}")
            inviteTopicsToInvitePublicKeys[inviteTopic] = accountId to publicKey // todo refactor inviteTopics
            onSuccess(publicKey.keyAsHex)
        }

        if (accountId.isValid()) {
            try {
                val storedPublicKey = keyManagementRepository.getPublicKey(accountId.getInviteTag())
                inviteTopicsToInvitePublicKeys[keyManagementRepository.getTopicFromKey(storedPublicKey)] = accountId to storedPublicKey // todo refactor inviteTopics
                onSuccess(storedPublicKey.keyAsHex)
            } catch (e: MissingKeyException) {
                val invitePublicKey = keyManagementRepository.generateAndStoreX25519KeyPair()

                val didJwt: String = didJwtRepository
                    .encodeDidJwt(getIdentityKeyPair(accountId), keyserverUrl, EncodeInviteKeyDidJwtPayloadUseCase(encodeX25519DidKey(invitePublicKey.keyAsBytes), accountId))
                    .getOrElse() { error ->
                        onFailure(error)
                        return@goPublic
                    }

                scope.launch {
                    supervisorScope {
                        registerInviteUseCase(didJwt).fold(
                            onSuccess = { onSuccess(invitePublicKey) },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                }
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun unregisterIdentity(
        accountId: AccountId,
        onSign: (String) -> Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        if (accountId.isValid()) {
            val identityKey = keyManagementRepository.generateAndStoreEd25519KeyPair()
            val didKey = encodeEd25519DidKey(identityKey.keyAsBytes)

            val domain = keyserverUrl.toDomain().getOrElse {
                onFailure(UnableToExtractDomainException("Unable to extract domain from: $keyserverUrl"))
                return@unregisterIdentity
            }

            val payload = Cacao.Payload(
                iss = encodeDidPkh(accountId.value),
                domain = domain,
                aud = keyserverUrl, version = CURRENT_VERSION,
                nonce = randomBytes(32).toString(), iat = SimpleDateFormat(ISO_8601_PATTERN, Locale.getDefault()).format(Calendar.getInstance().time),
                nbf = null, exp = null, statement = null, requestId = null, resources = listOf(didKey)
            )

            val message = payload.toCAIP122Message()
            val signature = onSign(message)

            val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)

            scope.launch {
                supervisorScope {
                    unregisterIdentityUseCase(cacao).fold(
                        onSuccess = {
                            logger.log("Unregister identity: $didKey")
                            //todo stop listening for invites
                            onSuccess(didKey)
                        },
                        onFailure = { error -> onFailure(error) }
                    )
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
                val didKey = encodeEd25519DidKey(identityKey.keyAsBytes)

                val domain = keyserverUrl.toDomain().getOrElse {
                    onFailure(UnableToExtractDomainException("Unable to extract domain from: $keyserverUrl"))
                    return@registerIdentity
                }

                val payload = Cacao.Payload(
                    iss = encodeDidPkh(accountId.value),
                    domain = domain,
                    aud = keyserverUrl, version = CURRENT_VERSION,
                    nonce = randomBytes(32).toString(), iat = SimpleDateFormat(ISO_8601_PATTERN, Locale.getDefault()).format(Calendar.getInstance().time),
                    nbf = null, exp = null, statement = null, requestId = null, resources = listOf(didKey)
                )

                val message = payload.toCAIP122Message()
                val signature = onSign(message)

                val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)

                scope.launch {
                    supervisorScope {
                        registerIdentityUseCase(cacao).fold(
                            onSuccess = {
                                if (!private) {
                                    keyManagementRepository.setKey(identityKey, accountId.getIdentityTag()) //todo duplicate
                                    goPublic(accountId, onSuccess = { inviteKey ->
                                        logger.log("Registered invite key: $inviteKey")
                                        onSuccess(identityKey)
                                    }, onFailure = { error -> onFailure(error) })
                                } else {
                                    onSuccess(identityKey)
                                }
                            },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                }

            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    private suspend fun resolveIdentity(identityDidKey: String): Result<AccountId> {
        val identityKey = identityDidKey.split(DID_DELIMITER).last()
        val cacao = identities[identityKey]
        if (cacao != null) return runCatching { AccountId(decodeDidPkh(cacao.payload.iss)) }
        return resolveIdentityUseCase(identityKey).mapCatching { response ->
            identities[identityKey] = response.cacao
            AccountId(decodeDidPkh(response.cacao.payload.iss))
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

    internal fun goPrivate(accountId: AccountId, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (accountId.isValid()) {
            try {
                val invitePublicKey = keyManagementRepository.getPublicKey(accountId.getInviteTag())
                inviteTopicsToInvitePublicKeys.remove(keyManagementRepository.getTopicFromKey(invitePublicKey))

                val didJwt: String = didJwtRepository
                    .encodeDidJwt(getIdentityKeyPair(accountId), keyserverUrl, EncodeInviteKeyDidJwtPayloadUseCase(invitePublicKey.keyAsHex, accountId))
                    .getOrElse() { error ->
                        onFailure(error)
                        return@goPrivate
                    }

                scope.launch {
                    supervisorScope {
                        unregisterInviteUseCase(didJwt).fold(
                            onSuccess = { _ -> onSuccess() },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                }
            } catch (e: MissingKeyException) {
                onFailure(InviteKeyNotFound("Unable to find stored invite key for $accountId"))
            }

        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun invite(invite: Invite, onSuccess: (Long) -> Unit, onFailure: (Throwable) -> Unit) {
        if (!ChatValidator.isInviteMessageValid(invite.message.value)) {
            return onFailure(InviteMessageTooLongException())
        }

        val decodedInviteePublicKey = decodeX25519DidKey(invite.inviteePublicKey)
        setContact(invite.inviteeAccount, decodedInviteePublicKey) { error ->
            logger.error("Error while adding new account: $error")
            onFailure(error)
            return@setContact //todo: this will not stop execution of invite. Refactor necessary
        }

        val inviterPublicKey = try {
            keyManagementRepository.generateAndStoreX25519KeyPair()
        } catch (e: Exception) {
            return onFailure(e)
        }

        try {
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviterPublicKey, decodedInviteePublicKey)
            val inviteTopic = keyManagementRepository.getTopicFromKey(decodedInviteePublicKey)
            keyManagementRepository.setKeyAgreement(inviteTopic, inviterPublicKey, decodedInviteePublicKey)

            val participants = Participants(senderPublicKey = inviterPublicKey, receiverPublicKey = decodedInviteePublicKey)
            val didJwt: String = didJwtRepository
                .encodeDidJwt(getIdentityKeyPair(invite.inviterAccount), keyserverUrl, EncodeInviteProposalDidJwtPayloadUseCase(inviterPublicKey, invite.inviteeAccount, invite.message.value))
                .getOrElse() { error -> return@invite onFailure(error) }

            val inviteParams = ChatParams.InviteParams(inviteAuth = didJwt)
            val inviteId = generateId()
            val payload = ChatRpc.ChatInvite(id = inviteId, params = inviteParams)
            val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)

            keyManagementRepository.setKey(symmetricKey, acceptTopic.value)
            jsonRpcInteractor.subscribe(acceptTopic) { error -> return@subscribe onFailure(error) }

            val irnParams = IrnParams(Tags.CHAT_INVITE, Ttl(MONTH_IN_SECONDS), true)
            jsonRpcInteractor.publishJsonRpcRequest(inviteTopic, irnParams, payload, EnvelopeType.ONE, participants,
                {
                    logger.log("Chat invite sent successfully")
                    onSuccess(inviteId)
                },
                { throwable ->
                    logger.log("Chat invite error: $throwable")
                    jsonRpcInteractor.unsubscribe(acceptTopic)
                    onFailure(throwable)
                }
            )
        } catch (error: Exception) {
            keyManagementRepository.removeKeys(inviterPublicKey.keyAsHex)
            onFailure(error)
        }
    }

    internal fun setContact(accountId: AccountId, publicInviteKey: PublicKey, onFailure: (Throwable) -> Unit) = try {
        if (chatStorage.doesContactNotExists(accountId)) {
            chatStorage.createContact(Contact(accountId, publicInviteKey, accountId.value.take(10)))
        } else {
            chatStorage.updateContact(accountId, publicInviteKey, accountId.value.take(10))
        }
    } catch (error: Exception) {
        onFailure(error)
    }

    internal fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        // Needs to be in scope to
        scope.launch {
            try {
                val request = inviteRequestMap[inviteId]?.first ?: throw GenericException("No request for inviteId")
                val claims = inviteRequestMap[inviteId]?.second ?: throw GenericException("No claims for inviteId")
                val inviterPublicKey = decodeX25519DidKey(claims.inviterPublicKey)
                inviteRequestMap.remove(inviteId)
                val inviteeAccountId = AccountId(decodeDidPkh(claims.audience))
                logger.log(claims.toString())
                val inviterAccountId = resolveIdentity(claims.issuer).getOrThrow()

                val inviteePublicKey = keyManagementRepository.getPublicKey(inviteeAccountId.getInviteTag())
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                keyManagementRepository.setKey(symmetricKey, acceptTopic.value)

                val publicKey = keyManagementRepository.generateAndStoreX25519KeyPair()

                val didJwt: String = didJwtRepository
                    .encodeDidJwt(getIdentityKeyPair(inviteeAccountId), keyserverUrl, EncodeInviteApprovalDidJwtPayloadUseCase(publicKey, inviterAccountId))
                    .getOrElse() { error ->
                        onFailure(error)
                        return@launch
                    }

                val acceptanceParams = CoreChatParams.AcceptanceParams(responseAuth = didJwt)
                val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))

                jsonRpcInteractor.respondWithParams(request.copy(topic = acceptTopic), acceptanceParams, irnParams, EnvelopeType.ZERO) { error -> return@respondWithParams onFailure(error) }

                val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, inviterPublicKey)
                val threadTopic = keyManagementRepository.getTopicFromKey(threadSymmetricKey)
                keyManagementRepository.setKey(threadSymmetricKey, threadTopic.value)
                threadsRepository.insertThread(threadTopic.value, selfAccount = inviteeAccountId.value, peerAccount = inviterAccountId.value)

                jsonRpcInteractor.subscribe(threadTopic) { error -> return@subscribe onFailure(error) }
                onSuccess(threadTopic.value)

            } catch (error: Exception) {
                onFailure(error)
            }
        }
    }

    internal fun reject(inviteId: Long, onFailure: (Throwable) -> Unit) {
        try {
            val request = inviteRequestMap[inviteId]?.first ?: throw GenericException("No request for inviteId")
            val claims = inviteRequestMap[inviteId]?.second ?: throw GenericException("No claims for inviteId")
            val inviterPublicKey = PublicKey(claims.inviterPublicKey)
            inviteRequestMap.remove(inviteId)
            val inviteeAccountId = AccountId(decodeDidPkh(claims.audience))

            val inviteePublicKey = keyManagementRepository.getPublicKey(inviteeAccountId.getInviteTag())
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
            val rejectTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, rejectTopic.value)

            val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))
            jsonRpcInteractor.respondWithError(
                request.copy(topic = rejectTopic),
                PeerError.UserRejectedInvitation("Invitation rejected by a user"),
                irnParams
            ) { throwable -> onFailure(throwable) }
        } catch (e: MissingKeyException) {
            return onFailure(e)
        }
    }

    internal fun message(topic: String, message: SendMessage, onFailure: (Throwable) -> Unit) {
        if (!ChatValidator.isChatMessageValid(message.message.value)) {
            return onFailure(ChatMessageTooLongException())
        } else if (!ChatValidator.isMediaDataValid(message.media?.data?.value)) {
            return onFailure(MediaDataTooLongException())
        }

        val thread = threadsRepository.getThreadByTopic(topic)
        val (authorAccountId, recipientAccountId) = AccountId(thread.selfAccount) to AccountId(thread.peerAccount)
        val didJwt: String = didJwtRepository
            .encodeDidJwt(getIdentityKeyPair(authorAccountId), keyserverUrl, EncodeChatMessageDidJwtPayloadUseCase(message.message.value, recipientAccountId, message.media))
            .getOrElse() { error -> return@message onFailure(error) }

        val messageParams = ChatParams.MessageParams(messageAuth = didJwt)
        val payload = ChatRpc.ChatMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParams(Tags.CHAT_MESSAGE, Ttl(MONTH_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload,
            onSuccess = { logger.log("Chat message sent successfully") },
            onFailure = { throwable ->
                logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        val payload = ChatRpc.ChatLeave(id = generateId(), params = ChatParams.LeaveParams())
        val irnParams = IrnParams(Tags.CHAT_LEAVE, Ttl(MONTH_IN_SECONDS), true)

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
        val claims = didJwtRepository.extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteProposal>(params.inviteAuth)
            .getOrElse() { error ->
                logger.error(error)
                return@onInviteRequest
            }
        inviteRequestMap[wcRequest.id] = wcRequest to claims

        scope.launch {
            val inviterAccountId = resolveIdentity(claims.issuer)
                .getOrElse() { error ->
                    logger.error(error)
                    return@launch
                }
            logger.log("Invite received. Resolved identity: $inviterAccountId")

            val (inviteeAccountId, inviteePublicKey) = inviteTopicsToInvitePublicKeys[wcRequest.topic]!!
            val invite = ReceivedInvite(wcRequest.id, inviterAccountId, inviteeAccountId, InviteMessage(claims.subject), claims.inviterPublicKey, inviteePublicKey.keyAsHex)
            _events.emit(Events.OnInvite(invite))
        }
        //TODO: Add adding invites to storage. For Alpha we will use only emitted event.
    }

    private fun onMessage(wcRequest: WCRequest, params: ChatParams.MessageParams) {
        logger.log("Message received")
        val claims = didJwtRepository.extractVerifiedDidJwtClaims<ChatDidJwtClaims.ChatMessage>(params.messageAuth)
            .getOrElse() { error ->
                logger.error(error)
                return@onMessage
            }

        scope.launch {
            val authorAccountId = resolveIdentity(claims.issuer)
                .getOrElse() { error ->
                    logger.error(error)
                    return@launch
                }
            val recipientAccountId = AccountId(decodeDidPkh(claims.audience))
            val message = Message(wcRequest.topic, ChatMessage(claims.subject), authorAccountId, wcRequest.id, claims.media)
            _events.emit(Events.OnMessage(message))

            val didJwt: String = didJwtRepository
                .encodeDidJwt(getIdentityKeyPair(recipientAccountId), keyserverUrl, EncodeChatReceiptDidJwtPayloadUseCase(claims.subject, authorAccountId))
                .getOrElse() { error ->
                    logger.error(error)
                    return@launch
                }

            val receiptParams = CoreChatParams.ReceiptParams(receiptAuth = didJwt)
            val irnParams = IrnParams(Tags.CHAT_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))

            jsonRpcInteractor.respondWithParams(wcRequest, receiptParams, irnParams, EnvelopeType.ZERO) { error -> logger.error(error) }
        }
        //TODO: Add adding messages to storage. For Alpha we will use only emitted event.
    }

    private fun onLeft(request: WCRequest) {
        threadsRepository.deleteThreadByTopic(request.topic.value)

        scope.launch {
            _events.emit(Events.OnLeft(request.topic.value))
            jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.CHAT_LEAVE_RESPONSE, Ttl(MONTH_IN_SECONDS)))
        }
    }

    private fun onPong(request: WCRequest) {
        jsonRpcInteractor.respondWithSuccess(request, IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS)))
    }

    private suspend fun onInviteResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                logger.log("Chat invite was rejected")
                scope.launch { _events.emit(Events.OnReject(wcResponse.topic.value)) }
            }
            is JsonRpcResponse.JsonRpcResult -> onInviteAccepted(response, wcResponse)
        }
    }

    private suspend fun onMessageResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                logger.log("Message error response: $response")
            }
            is JsonRpcResponse.JsonRpcResult -> {
                logger.log("Message success response")
                //todo validate receipt auth and notify that message was received. Needs discussion and specs (Milestone 2)
            }
        }
    }

    private fun onLeaveResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> logger.log("Chat leave error response: $response")
            is JsonRpcResponse.JsonRpcResult -> logger.log("Chat leave success response")
        }
    }

    private suspend fun onInviteAccepted(response: JsonRpcResponse.JsonRpcResult, wcResponse: WCResponse) {
        logger.log("Chat invite was accepted")
        val acceptParams = response.result as CoreChatParams.AcceptanceParams
        val claims = didJwtRepository.extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteApproval>(acceptParams.responseAuth)
            .getOrElse() { error ->
                logger.error(error)
                return@onInviteAccepted
            }
        logger.log(claims.toString())
        val peerPubKey = decodeX25519DidKey(claims.subject)

        try {
            val selfPubKey: PublicKey = keyManagementRepository.getSelfPublicFromKeyAgreement(wcResponse.topic)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, peerPubKey)
            val threadTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, threadTopic.value)

            val inviteeAccountId = resolveIdentity(claims.issuer).getOrThrow().value
            val inviterAccountId = decodeDidPkh(claims.audience)
            threadsRepository.insertThread(threadTopic.value, selfAccount = inviterAccountId, peerAccount = inviteeAccountId)

            jsonRpcInteractor.subscribe(threadTopic) { error ->
                scope.launch {
                    _events.emit(SDKError(error))
                }
                return@subscribe
            }

            scope.launch { _events.emit(Events.OnJoined(threadTopic.value)) }
        } catch (e: Exception) {
            scope.launch { _events.emit(SDKError(e)) }
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
                    is ChatParams.MessageParams -> onMessageResponse(response)
                    is ChatParams.LeaveParams -> onMessageResponse(response)
                }
            }
        }

    private fun trySubscribeToInviteTopic() {
        logger.log("trySubscribeToInviteTopic()")
        val inviteTopics = inviteTopicsToInvitePublicKeys.keys.map { topic -> topic.value }
        try {
            jsonRpcInteractor.batchSubscribe(inviteTopics) { error -> scope.launch { _events.emit(SDKError(error)) } }
        } catch (e: Exception) {
            scope.launch { _events.emit(SDKError(e)) }
        }
    }

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _events.emit(exception) }
            .launchIn(scope)

    private fun AccountId.getIdentityTag(): String = "$SELF_IDENTITY_PUBLIC_KEY_CONTEXT${this.value}"
    private fun AccountId.getInviteTag(): String = "$SELF_INVITE_PUBLIC_KEY_CONTEXT${this.value}"


    private fun String.toDomain(): Result<String> = runCatching {
        val uri = URI(this)
        val domain: String = uri.host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    private companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
    }
}