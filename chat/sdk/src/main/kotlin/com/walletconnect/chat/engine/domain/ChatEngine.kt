@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.utils.DAY_IN_SECONDS
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.android.impl.utils.SELF_INVITE_PUBLIC_KEY_CONTEXT
import com.walletconnect.android.impl.utils.SELF_PARTICIPANT_CONTEXT
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.scope
import com.walletconnect.chat.common.exceptions.InvalidAccountIdException
import com.walletconnect.chat.common.exceptions.PeerError
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterAccountUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveAccountUseCase
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.storage.ChatStorageRepository
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ChatEngine(
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val chatStorage: ChatStorageRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()
    private val inviteRequestMap: MutableMap<Long, WCRequest> = mutableMapOf()

    init {
        collectJsonRpcRequests()
        collectPeerResponses()
        resubscribeToInviteTopic()
    }

    fun handleInitializationErrors(onError: (WalletConnectException) -> Unit) {
        jsonRpcInteractor.initializationErrorsFlow.onEach { walletConnectException -> onError(walletConnectException) }.launchIn(scope)
    }

    internal fun registerAccount(
        accountId: AccountId,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        private: Boolean,
    ) {
        fun onSuccess(publicKey: PublicKey) {
            val topic = keyManagementRepository.getTopicFromKey(publicKey)
            keyManagementRepository.setKey(publicKey, SELF_INVITE_PUBLIC_KEY_CONTEXT)
            keyManagementRepository.setKey(publicKey, "$SELF_PARTICIPANT_CONTEXT${topic.value}")
            trySubscribeToInviteTopic()
            onSuccess(publicKey.keyAsHex)
        }

        if (accountId.isValid()) {
            val publicKey = keyManagementRepository.generateKeyPair()
            if (!private) {
                scope.launch {
                    supervisorScope {
                        registerAccountUseCase(AccountIdWithPublicKey(accountId, publicKey)).fold(
                            onSuccess = { onSuccess(publicKey) },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                }
            } else {
                onSuccess(publicKey)
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun resolveAccount(accountId: AccountId, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        if (accountId.isValid()) {
            //todo: check if there's pubKey under given accountId if yes use it if not register
            scope.launch {
                supervisorScope {
                    resolveAccountUseCase(accountId).fold(
                        onSuccess = { accountIdWithPublicKeyVO -> onSuccess(accountIdWithPublicKeyVO.publicKey.keyAsHex) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        } else {
            onFailure(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }

    internal fun invite(peerAccount: AccountId, invite: EngineDO.Invite, onFailure: (Throwable) -> Unit) = try {
        val senderPublicKey = keyManagementRepository.generateKeyPair()

        val contact = chatStorage.getContact(peerAccount)
        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(senderPublicKey, contact.publicKey)
        val inviteTopic = keyManagementRepository.getTopicFromKey(contact.publicKey)
        keyManagementRepository.setKeyAgreement(inviteTopic, senderPublicKey, contact.publicKey)

        val participants = Participants(senderPublicKey = senderPublicKey, receiverPublicKey = contact.publicKey)
        val inviteParams = ChatParams.InviteParams(invite.message, invite.accountId.value, senderPublicKey.keyAsHex, invite.signature)
        val payload = ChatRpc.ChatInvite(id = generateId(), params = inviteParams)
        val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)

        keyManagementRepository.setKey(symmetricKey, acceptTopic.value)
        jsonRpcInteractor.subscribe(acceptTopic)

        val irnParams = IrnParams(Tags.CHAT_INVITE, Ttl(DAY_IN_SECONDS), true)
        jsonRpcInteractor.publishJsonRpcRequest(inviteTopic, irnParams, payload, EnvelopeType.ONE, participants,
            { Logger.log("Chat invite sent successfully") },
            { throwable ->
                Logger.log("Chat invite error: $throwable")
                jsonRpcInteractor.unsubscribe(acceptTopic)
                onFailure(throwable)
            })

    } catch (error: Exception) {
        onFailure(error)
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

    internal fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) = try {
        val request = inviteRequestMap[inviteId] ?: throw GenericException("No request for inviteId")
        val senderPublicKey = PublicKey((request.params as ChatParams.InviteParams).publicKey)
        inviteRequestMap.remove(inviteId)

        val invitePublicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(invitePublicKey, senderPublicKey)
        val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
        keyManagementRepository.setKey(symmetricKey, acceptTopic.value)

        val publicKey = keyManagementRepository.generateKeyPair()
        val acceptanceParams = ChatParams.AcceptanceParams(publicKey.keyAsHex)
        val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.respondWithParams(request.copy(topic = acceptTopic), acceptanceParams, irnParams, EnvelopeType.ZERO)

        val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, senderPublicKey)
        val threadTopic = keyManagementRepository.getTopicFromKey(threadSymmetricKey)
        keyManagementRepository.setKey(threadSymmetricKey, threadTopic.value)
        jsonRpcInteractor.subscribe(threadTopic)
        onSuccess(threadTopic.value)
    } catch (error: Exception) {
        onFailure(error)
    }

    internal fun reject(inviteId: Long, onFailure: (Throwable) -> Unit) {
        val request = inviteRequestMap[inviteId] ?: throw GenericException("No request for inviteId")
        val senderPublicKey = PublicKey((request.params as ChatParams.InviteParams).publicKey)
        inviteRequestMap.remove(inviteId)

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
    }

    internal fun message(topic: String, sendMessage: EngineDO.SendMessage, onFailure: (Throwable) -> Unit) {
        //todo resolve AUTHOR_ACCOUNT from thread storage by topic
        val messageParams = ChatParams.MessageParams(sendMessage.message, sendMessage.author.value, System.currentTimeMillis(), sendMessage.media)
        val payload = ChatRpc.ChatMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParams(Tags.CHAT_MESSAGE, Ttl(DAY_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload,
            onSuccess = { Logger.log("Chat message sent successfully") },
            onFailure = { throwable ->
                Logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        //todo: correct define params
        val leaveParams = ChatParams.LeaveParams()
        val payload = ChatRpc.ChatLeave(id = generateId(), params = leaveParams)
        val irnParams = IrnParams(Tags.CHAT_LEAVE, Ttl(DAY_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, payload, EnvelopeType.ZERO,
            onSuccess = { Logger.log("Chat message sent successfully") },
            onFailure = { throwable ->
                Logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun ping(topic: String, onFailure: (Throwable) -> Unit) {
        //TODO
    }

    private fun collectJsonRpcRequests() {
        scope.launch {
            jsonRpcInteractor.clientSyncJsonRpc.collect { request ->
                when (val params = request.params) {
                    is ChatParams.InviteParams -> onInviteRequest(request, params)
                    is ChatParams.MessageParams -> onMessage(request, params)
                }
            }
        }
    }

    private fun onInviteRequest(wcRequest: WCRequest, params: ChatParams.InviteParams) {
        inviteRequestMap[wcRequest.id] = wcRequest
        scope.launch {
            val invite = EngineDO.Invite(AccountId(params.account), params.message, params.signature)
            _events.emit(EngineDO.Events.OnInvite(wcRequest.id, invite))
        }
        //TODO: Add adding invites to storage. For MVP we will use only emitted event.
    }

    private fun onMessage(wcRequest: WCRequest, params: ChatParams.MessageParams) {
        scope.launch {
            val message = EngineDO.Message(params.message, AccountId(params.authorAccount), params.timestamp, params.media)
            _events.emit(EngineDO.Events.OnMessage(wcRequest.topic.value, message))
        }
        //TODO: Add adding messages to storage. For MVP we will use only emitted event.
    }

    private fun collectPeerResponses() {
        scope.launch {
            jsonRpcInteractor.peerResponse.collect { response ->
                when (response.params) {
                    is ChatParams.InviteParams -> onInviteResponse(response)
                }
            }
        }
    }

    private fun onInviteResponse(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> {
                Logger.log("Chat invite was rejected")
                scope.launch { _events.emit(EngineDO.Events.OnReject(wcResponse.topic.value)) }
            }
            is JsonRpcResponse.JsonRpcResult -> {
                Logger.log("Chat invite was accepted")
                val acceptParams = response.result as ChatParams.AcceptanceParams
                val pubKeyZ = PublicKey(acceptParams.publicKey)
                val (selfPubKey, _) = keyManagementRepository.getKeyAgreement(wcResponse.topic)
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, pubKeyZ)
                val threadTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                keyManagementRepository.setKey(symmetricKey, threadTopic.value)
                jsonRpcInteractor.subscribe(threadTopic)
                scope.launch { _events.emit(EngineDO.Events.OnJoined(threadTopic.value)) }
            }
        }
    }

    private fun resubscribeToInviteTopic() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _events.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach { coroutineScope { launch(Dispatchers.IO) { trySubscribeToInviteTopic() } } }
            .launchIn(scope)
    }

    private fun trySubscribeToInviteTopic() {
        try {
            val publicKey = keyManagementRepository.getPublicKey(SELF_INVITE_PUBLIC_KEY_CONTEXT)
            val topic = keyManagementRepository.getTopicFromKey(publicKey)
            jsonRpcInteractor.subscribe(topic)
            Logger.log("Listening for invite on: $topic, pubKey X:$publicKey")
        } catch (error: Exception) {
            Logger.log(error) // It will log if run before registerAccount()
            //TODO: Create exception if there is no key created
        }
    }
}