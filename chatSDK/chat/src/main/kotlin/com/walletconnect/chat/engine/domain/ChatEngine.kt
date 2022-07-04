@file:JvmSynthetic

package com.walletconnect.chat.engine.domain

import com.walletconnect.chat.copiedFromSign.core.model.type.enums.EnvelopeType
import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.ParticipantsVO
import com.walletconnect.chat.copiedFromSign.core.scope.scope
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.copiedFromSign.json_rpc.domain.RelayerInteractor
import com.walletconnect.chat.copiedFromSign.util.Logger
import com.walletconnect.chat.copiedFromSign.util.generateId
import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.core.model.vo.EventsVO
import com.walletconnect.chat.core.model.vo.clientsync.ChatRpcVO
import com.walletconnect.chat.core.model.vo.clientsync.params.ChatParamsVO
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterAccountUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveAccountUseCase
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.engine.model.toMediaVO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ChatEngine(
    private val registerAccountUseCase: RegisterAccountUseCase,
    private val resolveAccountUseCase: ResolveAccountUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val relayer: RelayerInteractor,
) {
    private val _events: MutableSharedFlow<EventsVO> = MutableSharedFlow()
    val events: SharedFlow<EventsVO> = _events.asSharedFlow()

    init {
        collectJsonRpcRequests()
        collectPeerResponses()
        relayer.initializationErrorsFlow.onEach { error -> Logger.error(error) }.launchIn(scope)
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
        val (publicKey, _) = keyManagementRepository.getOrGenerateInviteSelfKeyPair()

        if (!private) {
            scope.launch {
                supervisorScope {
                    registerAccountUseCase(AccountIdWithPublicKeyVO(accountId, publicKey)).fold(
                        onSuccess = { onSuccess(publicKey.keyAsHex) },
                        onFailure = { error -> onFailure(error) }
                    )
                }
            }
        } else {
            onSuccess(publicKey.keyAsHex)
        }
    }

    internal fun invite(invite: EngineDO.Invite, onFailure: (Throwable) -> Unit) {
        //todo: correct define params
        val inviteTopic = TopicVO("")
        val publicKey = ""
        val participantsVO = ParticipantsVO(senderPublicKey = PublicKey(""), receiverPublicKey = PublicKey(""))
        val envelopeType = EnvelopeType.ONE

        val inviteParams = ChatParamsVO.InviteParams(invite.message, invite.account, publicKey, invite.signature)
        val payload = ChatRpcVO.ChatInvite(id = generateId(), params = inviteParams)
        relayer.publishJsonRpcRequests(inviteTopic, payload, envelopeType,
            { Logger.log("Chat invite sent successfully") },
            { throwable ->
                Logger.log("Chat invite error: $throwable")
                onFailure(throwable)
            }, participantsVO)
    }

    internal fun accept(inviteId: String, onFailure: (Throwable) -> Unit) {
//        //todo: correct define params
//        val envelopeType = EnvelopeType.ZERO
//        val publicKey = ""
//        val request = WCRequestVO()
//
//        val approvalParams = ChatParamsVO.ApprovalParams(publicKey)
//        relayer.respondWithParams(request, approvalParams, envelopeType)
    }

    internal fun reject(inviteId: String, onFailure: (Throwable) -> Unit) {
//        //todo: correct define params
//        val envelopeType = EnvelopeType.ZERO
//        val request = WCRequestVO()
//        val error = PeerError.Error("reason", 1)
//
//        relayer.respondWithError(request, error, envelopeType) { throwable -> onFailure(throwable) }
    }

    internal fun message(topic: String, sendMessage: EngineDO.SendMessage, onFailure: (Throwable) -> Unit) {
        //todo: correct define params
        val authorAccount = ""
        val envelopeType = EnvelopeType.ZERO
        val timestamp = 123345L

        val messageParams = ChatParamsVO.MessageParams(sendMessage.message, authorAccount, timestamp, sendMessage.media.toMediaVO())
        val payload = ChatRpcVO.ChatMessage(id = generateId(), params = messageParams)
        relayer.publishJsonRpcRequests(TopicVO(topic), payload, envelopeType,
            { Logger.log("Chat message sent successfully") },
            { throwable ->
                Logger.log("Chat message error: $throwable")
                onFailure(throwable)
            })
    }

    internal fun leave(topic: String, onFailure: (Throwable) -> Unit) {
        //todo: correct define params
        val envelopeType = EnvelopeType.ZERO

        val leaveParams = ChatParamsVO.LeaveParams()
        val payload = ChatRpcVO.ChatLeave(id = generateId(), params = leaveParams)
        relayer.publishJsonRpcRequests(TopicVO(topic), payload, envelopeType,
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
            relayer.clientSyncJsonRpc.collect {

            }
        }
    }

    private fun collectPeerResponses() {
        scope.launch {
            relayer.peerResponse.collect {

            }
        }
    }
}