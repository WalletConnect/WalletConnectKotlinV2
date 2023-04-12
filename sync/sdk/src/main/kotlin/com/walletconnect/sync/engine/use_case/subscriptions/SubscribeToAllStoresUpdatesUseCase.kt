package com.walletconnect.sync.engine.use_case.subscriptions

import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sync.storage.StoresStorageRepository

internal class SubscribeToAllStoresUpdatesUseCase(
    private val storesRepository: StoresStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
) {
    suspend operator fun invoke(onError: (Throwable) -> Unit) {
        runCatching { storesRepository.getAllTopics() }.fold(
            onSuccess = { topics -> topics.trySubscribeToAllStoreTopics(onError) },
            onFailure = { error -> onError(error) }
        )
    }

    private fun List<Topic>.trySubscribeToAllStoreTopics(onError: (Throwable) -> Unit) = runCatching {
        jsonRpcInteractor.batchSubscribe(this.map { it.value }, onFailure = { error -> onError(error) }, onSuccess = { topics -> logger.log("Listening for store updates on: $topics") })
    }.onFailure { error -> onError(error) }
}