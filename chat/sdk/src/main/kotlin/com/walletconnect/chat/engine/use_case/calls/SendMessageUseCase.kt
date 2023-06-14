package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.ChatMessageTooLongException
import com.walletconnect.chat.common.exceptions.MediaDataTooLongException
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.Message
import com.walletconnect.chat.common.model.SendMessage
import com.walletconnect.chat.engine.domain.ChatValidator
import com.walletconnect.chat.jwt.use_case.EncodeChatMessageDidJwtPayloadUseCase
import com.walletconnect.chat.storage.MessageStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId
import com.walletconnect.utils.extractTimestamp
import kotlinx.coroutines.launch


internal class SendMessageUseCase(
    private val keyserverUrl: String,
    private val logger: Logger,
    private val threadsRepository: ThreadsStorageRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val messageRepository: MessageStorageRepository,
) : SendMessageUseCaseInterface {

    override fun message(topic: String, message: SendMessage, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            if (!ChatValidator.isChatMessageValid(message.message.value)) {
                return@launch onError(ChatMessageTooLongException())
            } else if (!ChatValidator.isMediaDataValid(message.media?.data?.value)) {
                return@launch onError(MediaDataTooLongException())
            }

            val thread = threadsRepository.getThreadByTopic(topic)
            val (authorAccountId, recipientAccountId) = thread.selfAccount to thread.peerAccount
            val messageId = generateId()
            val messageTimestampInMs = messageId.extractTimestamp()
            val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(authorAccountId)

            val didJwt = encodeDidJwt(
                identityPrivateKey,
                EncodeChatMessageDidJwtPayloadUseCase(message.message.value, recipientAccountId, message.media, messageTimestampInMs),
                EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
            )
                .getOrElse() { error -> return@launch onError(error) }

            val messageParams = ChatParams.MessageParams(messageAuth = didJwt.value)
            val payload = ChatRpc.ChatMessage(id = messageId, params = messageParams)
            val irnParams = IrnParams(Tags.CHAT_MESSAGE, Ttl(MONTH_IN_SECONDS), true)

            messageRepository.insertMessage(Message(messageId, Topic(topic), message.message, authorAccountId, messageTimestampInMs, message.media))
            jsonRpcInteractor.publishJsonRpcRequest(
                Topic(topic), irnParams, payload,
                onSuccess = { onSuccess() },
                onFailure = { throwable ->
                    logger.error(throwable)
                    scope.launch { messageRepository.deleteMessageByMessageId(messageId) }
                    onError(throwable)
                })
        }
    }
}

internal interface SendMessageUseCaseInterface {
    fun message(topic: String, message: SendMessage, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
}