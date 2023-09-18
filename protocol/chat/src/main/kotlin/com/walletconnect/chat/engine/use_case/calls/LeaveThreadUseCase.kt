package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch


internal class LeaveThreadUseCase(
    private val logger: Logger,
    private val threadsRepository: ThreadsStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : LeaveThreadUseCaseInterface {
    override fun leave(topic: String, onError: (Throwable) -> Unit) {
        val payload = ChatRpc.ChatLeave(params = ChatParams.LeaveParams(), topic = topic)
        val irnParams = IrnParams(Tags.CHAT_LEAVE, Ttl(MONTH_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(topic), irnParams, payload, EnvelopeType.ZERO,
            onSuccess = {
                // Not sure if we want to remove thread and messages if someone leaves convo.
                // Maybe just forgetting thread symkey is better solution?
                scope.launch {
                    threadsRepository.deleteThreadByTopic(topic)
                    jsonRpcInteractor.unsubscribe(Topic(topic)) { error -> onError(error) }
                }
            },
            onFailure = { throwable -> onError(throwable).also { logger.error(throwable) } })
    }
}

internal interface LeaveThreadUseCaseInterface {
    fun leave(topic: String, onError: (Throwable) -> Unit)
}