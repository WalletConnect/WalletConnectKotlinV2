package com.walletconnect.sync.engine.use_case.come_up_with_good_name

import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sync.storage.StoresStorageRepository

internal class SubscribeToStoreUpdatesUseCase(
    private val storesRepository: StoresStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
) {
    suspend operator fun invoke(onError: (Throwable) -> Unit) {
        runCatching { storesRepository.getAllTopics() }.onSuccess { topics -> topics.trySubscribeToAllStoreTopics(onError) }
    }

    private fun List<Topic>.trySubscribeToAllStoreTopics(onError: (Throwable) -> Unit) = runCatching {
        jsonRpcInteractor.batchSubscribe(this.map { it.value }, onFailure = { error -> onError(error) }, onSuccess = { topics -> logger.log("Listening for store updates on: $topics") })
    }.onFailure { error -> onError(error) }
}