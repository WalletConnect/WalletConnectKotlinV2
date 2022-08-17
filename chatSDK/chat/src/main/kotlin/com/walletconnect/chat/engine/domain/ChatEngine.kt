@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.android_core.common.GenericException
import com.walletconnect.android_core.common.WalletConnectException
import com.walletconnect.chat.copiedFromSign.core.model.type.enums.EnvelopeType
import com.walletconnect.chat.copiedFromSign.core.model.vo.IrnParamsVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.TtlVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.ParticipantsVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.WCRequestVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.WCResponseVO
import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.copiedFromSign.json_rpc.domain.RelayerInteractor
import com.walletconnect.chat.copiedFromSign.util.Logger
import com.walletconnect.chat.copiedFromSign.util.generateId
import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.core.model.vo.Tags
import com.walletconnect.chat.core.model.vo.clientsync.ChatRpcVO
import com.walletconnect.chat.core.model.vo.clientsync.params.ChatParamsVO
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterAccountUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveAccountUseCase
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.storage.ChatStorageRepository
import com.walletconnect.sign.core.model.utils.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ChatEngine(
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val relayer: RelayerInteractor,
    private val chatStorage: ChatStorageRepository,
) {
    private val _events: MutableSharedFlow<EngineDO.Events> = MutableSharedFlow()
    val events: SharedFlow<EngineDO.Events> = _events.asSharedFlow()
    private val inviteRequestMap: MutableMap<Long, WCRequestVO> = mutableMapOf()

    init {
        collectJsonRpcRequests()
        collectPeerResponses()
        relayer.initializationErrorsFlow.onEach { error -> Logger.error(error) }.launchIn(scope)
        relayer.isConnectionAvailable
            .onEach { isAvailable ->
//                _events.emit(EngineDO.ConnectionState(isAvailable)) todo add connection state callbacks
            }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                coroutineScope {
                    launch(Dispatchers.IO) { trySubscribeToInviteTopic() }
                }
            }
            .launchIn(scope)

    }

    internal fun resolveAccount(accountId: AccountIdVO, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                resolveAccountUseCase(accountId).fold(
                    onSuccess = { accountIdWithPublicKeyVO -> onSuccess(accountIdWithPublicKeyVO.publicKey.keyAsHex) },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }

    internal fun registerAccount(
        accountId: AccountIdVO,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
        private: Boolean,
    ) {
        fun _onSuccess(publicKey: PublicKey) {
            val topic = TopicVO(keyManagementRepository.getHash(publicKey.keyAsHex))
            keyManagementRepository.setInviteSelfPublicKey(topic, publicKey)
            trySubscribeToInviteTopic()
            onSuccess(publicKey.keyAsHex)
        }

        val (publicKey, _) = keyManagementRepository.generateInviteSelfKeyPair()

        if (!private) {
            scope.launch {
                supervisorScope {
                    registerAccountUseCase(AccountIdWithPublicKeyVO(accountId, publicKey)).fold(
                        onSuccess = { _onSuccess(publicKey) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        } else {
            _onSuccess(publicKey)
        }
    }

    private fun trySubscribeToInviteTopic() {
        try {
            val publicKey = keyManagementRepository.getInviteSelfPublicKey()
            val topic = TopicVO(keyManagementRepository.getHash(publicKey.keyAsHex))
            relayer.subscribe(topic)
            Logger.log("Listening for invite on: $topic, pubKey X:$publicKey")
        } catch (error: Exception) {
            Logger.log(error) // It will log if run before registerAccount()
            //TODO: Create exception if there is no key created
        }
    }

    internal fun invite(peerAccount: AccountIdVO, invite: EngineDO.Invite, onFailure: (Throwable) -> Unit) = try {
        val senderPublicKey = keyManagementRepository.generateKeyPair() // KeyPair Y

        val contact = chatStorage.getContact(peerAccount)
        val publicKeyString = contact.public_key // TODO: What about camelCase?
        val receiverPublicKey = PublicKey(publicKeyString) // KeyPair X

        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey) // SymKey I
        val inviteTopic = TopicVO(keyManagementRepository.getHash(publicKeyString)) // Topic I
        keyManagementRepository.setKeyAgreement(inviteTopic, senderPublicKey, receiverPublicKey)

        val participantsVO = ParticipantsVO(senderPublicKey = senderPublicKey, receiverPublicKey = receiverPublicKey)

        val inviteParams = ChatParamsVO.InviteParams(invite.message, invite.accountId.value, senderPublicKey.keyAsHex, invite.signature)
        val payload = ChatRpcVO.ChatInvite(id = generateId(), params = inviteParams)

        val acceptTopic = TopicVO(keyManagementRepository.getHash(symmetricKey.keyAsHex))
        keyManagementRepository.setSymmetricKey(acceptTopic, symmetricKey)
        relayer.subscribe(acceptTopic)
        val irnParams = IrnParamsVO(Tags.CHAT_INVITE, TtlVO(Time.dayInSeconds), true)

        relayer.publishJsonRpcRequests(inviteTopic, irnParams, payload, EnvelopeType.ONE,
            {
                Logger.log("Chat invite sent successfully")
            },
            { throwable ->
                Logger.log("Chat invite error: $throwable")
                relayer.unsubscribe(acceptTopic)
                onFailure(throwable)
            }, participantsVO)

    } catch (error: Exception) {
        onFailure(error)
    }

    private fun onInviteResponse(wcResponse: WCResponseVO) {
        when (val response = wcResponse.response) {
            is JsonRpcResponseVO.JsonRpcError -> {
                Logger.log("Chat invite was rejected")
            }
            is JsonRpcResponseVO.JsonRpcResult -> {
                Logger.log("Chat invite was accepted")
                val acceptParams = response.result as ChatParamsVO.AcceptanceParams
                val pubKeyZ = PublicKey(acceptParams.publicKey) // PubKey Z
                val (selfPubKey, _) = keyManagementRepository.getKeyAgreement(wcResponse.topic)
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, pubKeyZ) // SymKey T
                val threadTopic = TopicVO(keyManagementRepository.getHash(symmetricKey.keyAsHex))
                keyManagementRepository.setSymmetricKey(threadTopic, symmetricKey)
                relayer.subscribe(threadTopic)
                scope.launch {
                    _events.emit(EngineDO.Events.OnJoined(threadTopic.value))
                }
            }
        }
    }

    private fun onInviteRequest(wcRequest: WCRequestVO, params: ChatParamsVO.InviteParams) {

        inviteRequestMap[wcRequest.id] = wcRequest // todo when to remove it?
        scope.launch {
            _events.emit(EngineDO.Events.OnInvite(wcRequest.id,
                EngineDO.Invite(AccountIdVO(params.account), params.message, params.signature)))
        }

        //TODO: Add adding invites to storage. For MVP we will use only emitted event.
    }


    internal fun accept(inviteId: Long, onFailure: (Throwable) -> Unit) = try {
        val request = inviteRequestMap[inviteId] ?: throw GenericException("No request for inviteId")
        val senderPublicKey = PublicKey((request.params as ChatParamsVO.InviteParams).publicKey) // PubKey Y
        inviteRequestMap.remove(inviteId)

        val invitePublicKey = keyManagementRepository.getInviteSelfPublicKey() // PubKey X
        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(invitePublicKey, senderPublicKey) // SymKey I
        val acceptTopic = TopicVO(keyManagementRepository.getHash(symmetricKey.keyAsHex)) // Topic T
        keyManagementRepository.setSymmetricKey(acceptTopic, symmetricKey)

        val publicKey = keyManagementRepository.generateKeyPair() // KeyPair Z


        val acceptanceParams = ChatParamsVO.AcceptanceParams(publicKey.keyAsHex)
        val irnParams = IrnParamsVO(Tags.CHAT_INVITE_RESPONSE, TtlVO(Time.dayInSeconds))

        relayer.respondWithParams(request.copy(topic = acceptTopic), acceptanceParams, irnParams, EnvelopeType.ZERO)

        val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, senderPublicKey) // SymKey T
        val threadTopic = TopicVO(keyManagementRepository.getHash(threadSymmetricKey.keyAsHex)) // Topic T
        keyManagementRepository.setSymmetricKey(threadTopic, threadSymmetricKey)
        relayer.subscribe(threadTopic)

        scope.launch {
            _events.emit(EngineDO.Events.OnJoined(threadTopic.value))
        }
    } catch (error: Exception) {
        onFailure(error)
    }


    internal fun reject(inviteId: String, onFailure: (Throwable) -> Unit) {
//        //todo: correct define params
//        val request = WCRequestVO()
//        val error = PeerError.Error("reason", 1)
//
//        relayer.respondWithError(request, error, EnvelopeType.ZERO) { throwable -> onFailure(throwable) }
    }

    internal fun message(topic: String, sendMessage: EngineDO.SendMessage, onFailure: (Throwable) -> Unit) {
        //todo resolve AUTHOR_ACCOUNT from thread storage by topic
        val messageParams =
            ChatParamsVO.MessageParams(sendMessage.message, sendMessage.author.value, System.currentTimeMillis(), sendMessage.media)
        val payload = ChatRpcVO.ChatMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParamsVO(Tags.CHAT_MESSAGE, TtlVO(Time.dayInSeconds), true)

        relayer.publishJsonRpcRequests(TopicVO(topic), irnParams, payload, EnvelopeType.ZERO,
            { Logger.log("Chat message sent successfully") },
            { throwable ->
                Logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    private fun onMessage(wcRequest: WCRequestVO, params: ChatParamsVO.MessageParams) {
        scope.launch {
            _events.emit(EngineDO.Events.OnMessage(wcRequest.topic.value,
                EngineDO.Message(params.message, AccountIdVO(params.authorAccount), params.timestamp, params.media)))
        }

        //TODO: Add adding messages to storage. For MVP we will use only emitted event.
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        //todo: correct define params
        val leaveParams = ChatParamsVO.LeaveParams()
        val payload = ChatRpcVO.ChatLeave(id = generateId(), params = leaveParams)
        val irnParams = IrnParamsVO(Tags.CHAT_LEAVE, TtlVO(Time.dayInSeconds), true)

        relayer.publishJsonRpcRequests(TopicVO(topic), irnParams, payload, EnvelopeType.ZERO,
            { Logger.log("Chat message sent successfully") },
            { throwable ->
                Logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun ping(topic: String, onFailure: (Throwable) -> Unit) {
        //TODO
    }

    private fun collectJsonRpcRequests() {
        scope.launch {
            relayer.clientSyncJsonRpc.collect { request ->
                when (val params = request.params) {
                    is ChatParamsVO.InviteParams -> onInviteRequest(request, params)
                    is ChatParamsVO.MessageParams -> onMessage(request, params)
                }
            }
        }
    }

    private fun collectPeerResponses() {
        scope.launch {
            relayer.peerResponse.collect { response ->
                when (val params = response.params) {
                    is ChatParamsVO.InviteParams -> onInviteResponse(response)
                }
            }
        }
    }

    internal fun addContact(accountIdWithPublicKeyVO: AccountIdWithPublicKeyVO, onFailure: (Throwable) -> Unit) = try {
        if (chatStorage.doesContactNotExists(accountIdWithPublicKeyVO.accountId)) {
            chatStorage.createContact(EngineDO.Contact(accountIdWithPublicKeyVO, accountIdWithPublicKeyVO.accountId.value))
        } else {
            chatStorage.updateContact(accountIdWithPublicKeyVO.accountId,
                accountIdWithPublicKeyVO.publicKey,
                accountIdWithPublicKeyVO.accountId.value)
        }
    } catch (error: Exception) {
        onFailure(error)
    }
}