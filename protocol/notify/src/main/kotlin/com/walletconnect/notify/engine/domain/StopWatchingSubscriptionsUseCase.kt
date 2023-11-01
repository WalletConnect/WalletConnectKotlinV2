@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import kotlinx.coroutines.supervisorScope

internal class StopWatchingSubscriptionsUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
) {

    suspend operator fun invoke(accountId: AccountId, onFailure: (Throwable) -> Unit) = supervisorScope {
        val watchTopic = runCatching { registeredAccountsRepository.getAccountByAccountId(accountId.value).notifyServerWatchTopic }
            .getOrElse { error -> return@supervisorScope onFailure(error) }

        if (watchTopic == null) {
            return@supervisorScope onFailure(IllegalStateException("Watch topic is null"))
        } else {
            jsonRpcInteractor.unsubscribe(watchTopic) { error -> onFailure(error) }
        }
    }
}
