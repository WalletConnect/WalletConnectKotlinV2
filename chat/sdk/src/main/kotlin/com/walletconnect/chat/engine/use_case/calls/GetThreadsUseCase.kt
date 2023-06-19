package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.storage.ThreadsStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking


internal class GetThreadsUseCase(
    private val threadsRepository: ThreadsStorageRepository,
) : GetThreadsUseCaseInterface {

    override fun getThreads(accountId: String): Map<String, Thread> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            threadsRepository.getThreadsForSelfAccount(accountId).associateBy { thread -> thread.topic.value }
        }
    }

}

internal interface GetThreadsUseCaseInterface {
    fun getThreads(accountId: String): Map<String, Thread>
}