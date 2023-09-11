@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import kotlinx.coroutines.supervisorScope

internal class WatchSubscriptionsForEveryRegisteredAccountUseCase(
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val logger: Logger,
) {

    suspend operator fun invoke() = supervisorScope {
        val registeredAccounts = runCatching { registeredAccountsRepository.getAllAccounts() }
            .getOrElse { error -> return@supervisorScope logger.log("WatchSubscriptionsForEveryRegisteredAccountUseCase - getAllAccounts: $error") }
        logger.log("WatchSubscriptionsForEveryRegisteredAccountUseCase - accounts(${registeredAccounts.size}): $registeredAccounts")

        registeredAccounts.forEach { registeredAccount ->
            watchSubscriptionsUseCase(registeredAccount.accountId, {
                logger.log("WatchSubscriptionsForEveryRegisteredAccountUseCase - onSuccess: $registeredAccount")
            }, { error ->
                logger.log("WatchSubscriptionsForEveryRegisteredAccountUseCase - onFailure: $registeredAccount, $error")
            })
        }

    }
}
