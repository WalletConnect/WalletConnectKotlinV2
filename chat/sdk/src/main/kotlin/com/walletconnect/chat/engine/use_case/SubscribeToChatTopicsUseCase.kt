package com.walletconnect.chat.engine.use_case

import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class SubscribeToChatTopicsUseCase(
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val accountsRepository: AccountsStorageRepository,
    private val threadsRepository: ThreadsStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) {
    private val _errors: MutableSharedFlow<SDKError> = MutableSharedFlow()
    val errors: SharedFlow<SDKError> = _errors.asSharedFlow()

    suspend operator fun invoke() {
        coroutineScope {
            launch(Dispatchers.IO) {
                trySubscribeToInviteTopics()
                trySubscribeToPendingAcceptTopics()
                trySubscribeToThreadTopics()
            }
        }
    }

    private suspend fun trySubscribeToInviteTopics() = accountsRepository.getAllInviteTopics()
        .trySubscribeToTopics("invite") { error -> scope.launch { _errors.emit(SDKError(error)) } }

    private suspend fun trySubscribeToThreadTopics() = threadsRepository.getAllThreads()
        .map { it.topic }
        .trySubscribeToTopics("thread messages") { error -> scope.launch { _errors.emit(SDKError(error)) } }

    private suspend fun trySubscribeToPendingAcceptTopics() = invitesRepository.getAllPendingSentInvites()
        .map { it.acceptTopic }
        .trySubscribeToTopics(topicDescription = "invite response") { error -> scope.launch { _errors.emit(SDKError(error)) } }

    private fun List<Topic>.trySubscribeToTopics(topicDescription: String, onError: (Throwable) -> Unit) = runCatching {
        jsonRpcInteractor.batchSubscribe(this.map { it.value }, onFailure = { error -> onError(error) }, onSuccess = { topics -> logger.log("Listening for $topicDescription on: $topics") })
    }.onFailure { error -> onError(error) }
}