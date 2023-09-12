package com.walletconnect.notify.engine.domain

import com.walletconnect.android.archive.ArchiveInterface
import com.walletconnect.android.archive.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.MessagesRepository

internal class FetchAllMessagesFromArchiveUseCase(
    private val archiveInterface: ArchiveInterface,
    private val messagesRepository: MessagesRepository,
    private val logger: Logger,
) {

    suspend operator fun invoke(subscription: Subscription.Active, onSuccess: suspend (List<NotifyRecord>) -> Unit, onError: (Throwable) -> Unit) {
        archiveInterface.getAllMessages(
            MessagesParams(subscription.notifyTopic.value, null, ArchiveInterface.DEFAULT_BATCH_SIZE, null),
            onError = { error -> onError(error.throwable) },
            onSuccess = { rpcList: Map<ClientJsonRpc, ClientParams> ->
                logger.log("Fetched ${rpcList.size} for topic: ${subscription.notifyTopic}")
                logger.log("Fetched $rpcList")

                val params = rpcList.map { (rpc, params) -> rpc.id to params as CoreNotifyParams.MessageParams }

                val messages = params.map { (id, messageParams) ->
                    val messageJwt = extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(messageParams.messageAuth).getOrThrow()
                    NotifyRecord(
                        id = id,
                        topic = subscription.notifyTopic.value,
                        publishedAt = id,
                        notifyMessage = NotifyMessage(
                            title = messageJwt.message.title,
                            body = messageJwt.message.body,
                            icon = messageJwt.message.icon,
                            url = messageJwt.message.url,
                            type = messageJwt.message.type
                        )
                    )
                }
                messagesRepository.setMessagesForSubscription(subscription.notifyTopic.value, messages)
                onSuccess(messages)
            }
        )
    }
}