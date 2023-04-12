package com.walletconnect.sync.engine.use_case.come_up_with_good_name

import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger

internal class SubscribeToStoreUpdatesUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
) {
    suspend operator fun invoke(storeTopic: Topic, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runCatching {
            jsonRpcInteractor.subscribe(
                storeTopic,
                onFailure = { error -> onError(error) },
                onSuccess = { topic -> onSuccess().also { logger.log("Listening for store updates on: $topic") } }
            )
        }.onFailure { error -> onError(error) }
    }
}