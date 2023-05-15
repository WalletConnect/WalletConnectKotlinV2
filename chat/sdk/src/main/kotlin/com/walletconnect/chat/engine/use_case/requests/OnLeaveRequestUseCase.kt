package com.walletconnect.chat.engine.use_case.requests

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.storage.MessageStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.Ttl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnLeaveRequestUseCase(
    private val messageRepository: MessageStorageRepository,
    private val threadsRepository: ThreadsStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    operator fun invoke(wcRequest: WCRequest) {
        // Not sure if we want to remove thread and messages if someone leaves convo.
        // Maybe just forgetting thread symkey is better solution?
        scope.launch {
            threadsRepository.deleteThreadByTopic(wcRequest.topic.value)
            messageRepository.deleteMessagesByTopic(wcRequest.topic.value)
        }

        scope.launch {
            _events.emit(Events.OnLeft(wcRequest.topic.value))
            jsonRpcInteractor.respondWithSuccess(wcRequest, IrnParams(Tags.CHAT_LEAVE_RESPONSE, Ttl(MONTH_IN_SECONDS)))
        }
    }
}