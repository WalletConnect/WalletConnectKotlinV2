@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.Cacao.Payload.Companion.CURRENT_VERSION
import com.walletconnect.android.internal.common.cacao.Cacao.Payload.Companion.ISO_8601_PATTERN
import com.walletconnect.android.internal.common.cacao.CacaoType
import com.walletconnect.android.internal.common.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.cacao.toCAIP122Message
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.*
import com.walletconnect.android.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.android.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.android.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.android.keyserver.domain.use_case.ResolveInviteUseCase
import com.walletconnect.android.keyserver.domain.use_case.UnregisterIdentityUseCase
import com.walletconnect.android.keyserver.domain.use_case.UnregisterInviteUseCase
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.chat.common.exceptions.*
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.*
import com.walletconnect.chat.json_rpc.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.chat.json_rpc.JsonRpcMethod
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.jwt.DidJwtRepository
import com.walletconnect.chat.jwt.use_case.*
import com.walletconnect.chat.storage.*
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
    private val projectId: ProjectId,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val unregisterIdentityUseCase: UnregisterIdentityUseCase,
    private val resolveIdentityUseCase: ResolveIdentityUseCase,
    private val registerInviteUseCase: RegisterInviteUseCase,
    private val unregisterInviteUseCase: UnregisterInviteUseCase,
    private val resolveInviteUseCase: ResolveInviteUseCase,
    private val didJwtRepository: DidJwtRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val contactRepository: ContactStorageRepository,
    private val pairingHandler: PairingControllerInterface,
    private val threadsRepository: ThreadsStorageRepository,
    private val invitesRepository: InvitesStorageRepository,
    private val messageRepository: MessageStorageRepository,
    private val accountsRepository: AccountsStorageRepository,
    private val identitiesRepository: IdentitiesStorageRepository,
    private val logger: Logger,
) {
    private var jsonRpcRequestsJob: Job? = null
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

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
                        trySubscribeToInviteTopics()
                        trySubscribeToPendingAcceptTopics()
                        trySubscribeToThreadTopics()
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
            scope.launch { accountsRepository.setAccountPublicInviteKey(accountId, publicKey, inviteTopic) }
            jsonRpcInteractor.subscribe(inviteTopic)
            onSuccess(publicKey.keyAsHex)
        }

        if (accountId.isValid()) {
            try {
                val storedPublicKey = keyManagementRepository.getPublicKey(accountId.getInviteTag())
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
        onSign: (String) -> Cacao.Signature?,
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
            val signature = onSign(message) ?: return

            val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)

            scope.launch {
                supervisorScope {
                    unregisterIdentityUseCase(cacao).fold(
                        onSuccess = {
                            logger.log("Unregister identity: $didKey")
                            val account = accountsRepository.getAccountByAccountId(accountId)
                            if (account.publicInviteKey != null && account.inviteTopic != null) {
                                keyManagementRepository.removeKeys(accountId.getInviteTag())
                                keyManagementRepository.removeKeys("$SELF_PARTICIPANT_CONTEXT${account.inviteTopic}")
                                jsonRpcInteractor.unsubscribe(account.inviteTopic)
                            }
                            accountsRepository.deleteAccountByAccountId(accountId)
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
        onSign: (String) -> Cacao.Signature?,
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
                val signature = onSign(message) ?: return

                val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)

                scope.launch {
                    supervisorScope {
                        registerIdentityUseCase(cacao).fold(
                            onSuccess = {
                                accountsRepository.upsertAccount(Account(accountId, identityKey, null, null))
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

    //todo extract to core
    private suspend fun resolveIdentity(identityDidKey: String): Result<AccountId> {
        val identityKey = identityDidKey.split(DID_DELIMITER).last()
        return runCatching { identitiesRepository.getAccountId(identityKey) }.onFailure {
            return resolveIdentityUseCase(identityKey).mapCatching { response ->
                if (CacaoVerifier(projectId).verify(response.cacao)) {
                    AccountId(decodeDidPkh(response.cacao.payload.iss)).also { accountId ->
                        identitiesRepository.insertIdentity(identityKey, accountId)
                    }
                } else {
                    throw Throwable("Invalid cacao")
                }
            }
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

                val didJwt: String = didJwtRepository
                    .encodeDidJwt(getIdentityKeyPair(accountId), keyserverUrl, EncodeInviteKeyDidJwtPayloadUseCase(invitePublicKey.keyAsHex, accountId))
                    .getOrElse() { error ->
                        onFailure(error)
                        return@goPrivate
                    }

                scope.launch {
                    supervisorScope {
                        unregisterInviteUseCase(didJwt).fold(
                            onSuccess = {
                                accountsRepository.removeAccountPublicInviteKey(accountId)
                                keyManagementRepository.removeKeys(accountId.getInviteTag())
                                val inviteTopic = keyManagementRepository.getTopicFromKey(invitePublicKey)
                                keyManagementRepository.removeKeys("$SELF_PARTICIPANT_CONTEXT${inviteTopic.value}")
                                jsonRpcInteractor.unsubscribe(inviteTopic)
                                onSuccess()
                            },
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

    internal fun invite(invite: SendInvite, onSuccess: (Long) -> Unit, onFailure: (Throwable) -> Unit) {
        if (!ChatValidator.isInviteMessageValid(invite.message.value)) {
            return onFailure(InviteMessageTooLongException())
        }

        if (invitesRepository.checkIfAccountsHaveExistingInvite(invite.inviterAccount.value, invite.inviteeAccount.value)) {
            return onFailure(AccountsAlreadyHaveInviteException)
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
                    scope.launch {
                        invitesRepository.insertInvite(
                            Invite.Sent(
                                inviteId, invite.inviterAccount, invite.inviteeAccount, invite.message,
                                inviterPublicKey, decodedInviteePublicKey, InviteStatus.PENDING, acceptTopic
                            )
                        )
                    }
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
        scope.launch { contactRepository.upsertContact(Contact(accountId, publicInviteKey, accountId.value)) }
    } catch (error: Exception) {
        onFailure(error)
    }

    internal fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            try {
                val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(inviteId)

                if (jsonRpcHistoryEntry == null) {
                    logger.error(MissingInviteRequestException.message)
                    return@launch onFailure(MissingInviteRequestException)
                }

                val invite = invitesRepository.getReceivedInviteByInviteId(inviteId)
                val inviterPublicKey = invite.inviterPublicKey
                val inviteeAccountId = invite.inviteeAccount
                val inviterAccountId = invite.inviterAccount

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
                val responseParams = JsonRpcResponse.JsonRpcResult(jsonRpcHistoryEntry.id, result = acceptanceParams)
                val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))
                jsonRpcInteractor.publishJsonRpcResponse(acceptTopic, irnParams, responseParams, {}, { error -> return@publishJsonRpcResponse onFailure(error) })

                val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, inviterPublicKey)
                val threadTopic = keyManagementRepository.getTopicFromKey(threadSymmetricKey)
                keyManagementRepository.setKey(threadSymmetricKey, threadTopic.value)

                threadsRepository.insertThread(threadTopic.value, selfAccount = inviteeAccountId.value, peerAccount = inviterAccountId.value)
                invitesRepository.updateStatusByInviteId(inviteId, InviteStatus.APPROVED)

                jsonRpcInteractor.subscribe(threadTopic) { error -> return@subscribe onFailure(error) }
                onSuccess(threadTopic.value)

            } catch (error: Exception) {
                onFailure(error)
            }
        }
    }

    internal fun reject(inviteId: Long, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            try {
                val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(inviteId)

                if (jsonRpcHistoryEntry == null) {
                    logger.error(MissingInviteRequestException.message)
                    return@launch onFailure(MissingInviteRequestException)
                }

                val invite = invitesRepository.getReceivedInviteByInviteId(inviteId)
                val inviterPublicKey = invite.inviterPublicKey
                val inviteeAccountId = invite.inviteeAccount

                val inviteePublicKey = keyManagementRepository.getPublicKey(inviteeAccountId.getInviteTag())
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val rejectTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                keyManagementRepository.setKey(symmetricKey, rejectTopic.value)

                val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))
                val peerError = PeerError.UserRejectedInvitation("Invitation rejected by a user")
                val responseParams = JsonRpcResponse.JsonRpcError(jsonRpcHistoryEntry.id, error = JsonRpcResponse.Error(peerError.code, peerError.message))
                jsonRpcInteractor.publishJsonRpcResponse(rejectTopic, irnParams, responseParams, {}, { error -> return@publishJsonRpcResponse onFailure(error) })
                invitesRepository.updateStatusByInviteId(inviteId, InviteStatus.REJECTED)
                onSuccess()
            } catch (e: MissingKeyException) {
                return@launch onFailure(e)
            }
        }
    }

    internal fun message(topic: String, message: SendMessage, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            if (!ChatValidator.isChatMessageValid(message.message.value)) {
                return@launch onFailure(ChatMessageTooLongException())
            } else if (!ChatValidator.isMediaDataValid(message.media?.data?.value)) {
                return@launch onFailure(MediaDataTooLongException())
            }

            val thread = threadsRepository.getThreadByTopic(topic)
            val (authorAccountId, recipientAccountId) = thread.selfAccount to thread.peerAccount
            val messageTimestampInMs = System.currentTimeMillis()

            val didJwt: String = didJwtRepository
                .encodeDidJwt(getIdentityKeyPair(authorAccountId), keyserverUrl, EncodeChatMessageDidJwtPayloadUseCase(message.message.value, recipientAccountId, message.media, messageTimestampInMs))
                .getOrElse() { error -> return@launch onFailure(error) }

            val messageParams = ChatParams.MessageParams(messageAuth = didJwt)
            val messageId = generateId()
            val payload = ChatRpc.ChatMessage(id = messageId, params = messageParams)
            val irnParams = IrnParams(Tags.CHAT_MESSAGE, Ttl(MONTH_IN_SECONDS), true)

            messageRepository.insertMessage(Message(messageId, Topic(topic), message.message, authorAccountId, messageTimestampInMs, message.media))
            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload,
                onSuccess = {
                    logger.log("Chat message sent successfully")
                    onSuccess()
                },
                onFailure = { throwable ->
                    logger.log("Chat message error: $throwable")
                    scope.launch { messageRepository.deleteMessageByMessageId(messageId) }
                    onFailure(throwable)
                })
        }
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        val payload = ChatRpc.ChatLeave(id = generateId(), params = ChatParams.LeaveParams())
        val irnParams = IrnParams(Tags.CHAT_LEAVE, Ttl(MONTH_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload, EnvelopeType.ZERO,
            onSuccess = {
                logger.log("Chat leave sent successfully")
                // Not sure if we want to remove thread and messages if someone leaves convo.
                // Maybe just forgetting thread symkey is better solution?
                scope.launch {
                    threadsRepository.deleteThreadByTopic(topic)
                    messageRepository.deleteMessagesByTopic(topic)
                    jsonRpcInteractor.unsubscribe(Topic(topic)) { error -> onFailure(error) }
                }
            },
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

    private fun <T> runBlockingInNewScope(block: suspend CoroutineScope.() -> T): T {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            block()
        }
    }

    internal fun getThreadsByAccount(accountId: String): Map<String, Thread> = runBlockingInNewScope() {
        threadsRepository.getThreadsForSelfAccount(accountId).associateBy { thread -> thread.topic.value }
    }

    internal fun getMessagesByTopic(topic: String): List<Message> = runBlockingInNewScope() {
        messageRepository.getMessageByTopic(topic)
    }

    internal fun getSentInvites(inviterAccountId: String): Map<Long, Invite.Sent> = runBlockingInNewScope() {
        invitesRepository.getSentInvitesForInviterAccount(inviterAccountId).associateBy { invite -> invite.id }
    }

    internal fun getReceivedInvites(inviteeAccountId: String): Map<Long, Invite.Received> = runBlockingInNewScope() {
        invitesRepository.getReceivedInvitesForInviteeAccount(inviteeAccountId).associateBy { invite -> invite.id }
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

        scope.launch {
            val inviterAccountId = resolveIdentity(claims.issuer)
                .getOrElse() { error ->
                    logger.error(error)
                    return@launch
                }
            logger.log("Invite received. Resolved identity: $inviterAccountId")


            runCatching { accountsRepository.getAccountByInviteTopic(wcRequest.topic) }.fold(onSuccess = { inviteeAccount ->
                if (invitesRepository.checkIfAccountsHaveExistingInvite(inviterAccountId.value, inviteeAccount.accountId.value)) {
                    logger.error(AccountsAlreadyHaveInviteException)
                    return@launch
                }

                val inviteePublicKey = inviteeAccount.publicInviteKey ?: throw Throwable("Missing publicInviteKey")
                val inviterPublicKey = decodeX25519DidKey(claims.inviterPublicKey)
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                val invite = Invite.Received(
                    wcRequest.id, inviterAccountId, inviteeAccount.accountId, InviteMessage(claims.subject),
                    inviterPublicKey, inviteePublicKey, InviteStatus.PENDING, acceptTopic
                )

                invitesRepository.insertInvite(invite)
                _events.emit(Events.OnInvite(invite))
            }, onFailure = { error -> logger.error(error) })

        }
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

            // Currently timestamps are based on claims issuedAt. Which MUST be changed to achieve proper order of messages.
            // Should be changed with specs: https://github.com/WalletConnect/walletconnect-docs/pull/473.
            // Change: Instead of claims.issuedAt use wcRequest.receivedAt

            val message = Message(wcRequest.id, wcRequest.topic, ChatMessage(claims.subject), authorAccountId, claims.issuedAt, claims.media)
            messageRepository.insertMessage(message)
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
    }

    private fun onLeft(request: WCRequest) {
        // Not sure if we want to remove thread and messages if someone leaves convo.
        // Maybe just forgetting thread symkey is better solution?
        scope.launch {
            threadsRepository.deleteThreadByTopic(request.topic.value)
            messageRepository.deleteMessagesByTopic(request.topic.value)
        }

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
            is JsonRpcResponse.JsonRpcError -> onInviteRejected(wcResponse)
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

    private fun onInviteRejected(wcResponse: WCResponse) {
        logger.log("Chat invite was rejected")
        scope.launch {
            invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.REJECTED)
            val inviteSent = invitesRepository.getSentInviteByInviteId(wcResponse.response.id)
            _events.emit(Events.OnInviteRejected(inviteSent))
        }
    }

    private suspend fun onInviteAccepted(response: JsonRpcResponse.JsonRpcResult, wcResponse: WCResponse) {
        logger.log("Chat invite was accepted")
        val acceptParams = response.result as CoreChatParams.AcceptanceParams
        val claims = didJwtRepository.extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteApproval>(acceptParams.responseAuth)
            .getOrElse() { error ->
                logger.error(error)
//                Discuss what state is invite in if not verified
//                invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.?????????)
                return@onInviteAccepted
            }
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

            invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.APPROVED)
            val inviteSent = invitesRepository.getSentInviteByInviteId(wcResponse.response.id)
            _events.emit(Events.OnInviteAccepted(threadTopic.value, inviteSent))
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


    private suspend fun trySubscribeToInviteTopics() = accountsRepository.getAllInviteTopics()
        .trySubscribeToTopics("invite") { error -> scope.launch { _events.emit(SDKError(error)) } }

    private suspend fun trySubscribeToThreadTopics() = threadsRepository.getAllThreads()
        .map { it.topic }
        .trySubscribeToTopics("thread messages") { error -> scope.launch { _events.emit(SDKError(error)) } }

    private suspend fun trySubscribeToPendingAcceptTopics() = invitesRepository.getAllPendingSentInvites()
        .map { it.acceptTopic }
        .trySubscribeToTopics(topicDescription = "invite response") { error -> scope.launch { _events.emit(SDKError(error)) } }

    private fun List<Topic>.trySubscribeToTopics(topicDescription: String, onError: (Throwable) -> Unit) = runCatching {
        jsonRpcInteractor.batchSubscribe(this.map { it.value }, onFailure = { error -> onError(error) }, onSuccess = { topics -> logger.log("Listening for $topicDescription on: $topics") })
    }.onFailure { error -> onError(error) }

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