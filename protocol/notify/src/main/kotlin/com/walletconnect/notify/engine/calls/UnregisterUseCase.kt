@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.StopWatchingSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

internal class UnregisterUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val keyserverUrl: String,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val stopWatchingSubscriptionsUseCase: StopWatchingSubscriptionsUseCase,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val messagesRepository: MessagesRepository,
) : UnregisterUseCaseInterface {

    override suspend fun unregister(
        account: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val accountId = AccountId(account)
        identitiesInteractor.unregisterIdentity(accountId, keyserverUrl).fold(
            onFailure = { error -> onFailure(error) },
            onSuccess = { identityPublicKey ->
                runCatching { registeredAccountsRepository.deleteAccountByAccountId(account) }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = {
                        stopWatchingSubscriptionsUseCase(accountId, onFailure)
                        subscriptionRepository.getAccountActiveSubscriptions(accountId).map { it.notifyTopic.value }.map { topic ->
                            jsonRpcInteractor.unsubscribe(Topic(topic)) { error -> onFailure(error) }
                            subscriptionRepository.deleteSubscriptionByNotifyTopic(topic)
                            messagesRepository.deleteMessagesByTopic(topic)
                        }
                        onSuccess(identityPublicKey.keyAsHex)
                    }
                )
            }
        )
    }
}

internal interface UnregisterUseCaseInterface {
    suspend fun unregister(
        account: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}