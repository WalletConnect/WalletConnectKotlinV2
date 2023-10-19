@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.notify.common.NotifyServerUrl
import kotlinx.coroutines.supervisorScope

internal class StopWatchingSubscriptionsUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val keyManagementRepository: KeyManagementRepository,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val getSelfKeyForWatchSubscriptionUseCase: GetSelfKeyForWatchSubscriptionUseCase,
    private val notifyServerUrl: NotifyServerUrl,
) {

    suspend operator fun invoke(accountId: AccountId, onFailure: (Throwable) -> Unit) = supervisorScope {
        val (peerPublicKey, _) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrElse { useCaseError -> return@supervisorScope onFailure(useCaseError) }

        val requestTopic = keyManagementRepository.getTopicFromKey(peerPublicKey)

        val selfPublicKey = getSelfKeyForWatchSubscriptionUseCase(requestTopic, accountId)
        val responseTopic = keyManagementRepository.generateTopicFromKeyAgreement(selfPublicKey, peerPublicKey)

        jsonRpcInteractor.unsubscribe(responseTopic) { error -> onFailure(error) }
    }
}
