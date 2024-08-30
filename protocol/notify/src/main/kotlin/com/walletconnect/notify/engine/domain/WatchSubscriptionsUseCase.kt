@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.NotifyServerUrl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import kotlinx.coroutines.supervisorScope

internal class WatchSubscriptionsUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val keyManagementRepository: KeyManagementRepository,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val getSelfKeyForWatchSubscriptionUseCase: GetSelfKeyForWatchSubscriptionUseCase,
    private val notifyServerUrl: NotifyServerUrl,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
) {

    suspend operator fun invoke(accountId: AccountId, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val (peerPublicKey, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrElse { useCaseError -> return@supervisorScope onFailure(useCaseError) }

        val requestTopic = keyManagementRepository.getTopicFromKey(peerPublicKey)

        val selfPublicKey = getSelfKeyForWatchSubscriptionUseCase(requestTopic, accountId)
        val responseTopic = keyManagementRepository.generateTopicFromKeyAgreement(selfPublicKey, peerPublicKey)

        jsonRpcInteractor.subscribe(responseTopic) { error -> onFailure(error) }

        val account = registeredAccountsRepository.getAccountByAccountId(accountId.value)
        val didJwt = fetchDidJwtInteractor.watchSubscriptionsRequest(accountId, authenticationPublicKey, account.appDomain)
            .getOrElse { error -> return@supervisorScope onFailure(error) }

        registeredAccountsRepository.updateNotifyServerData(accountId, responseTopic, authenticationPublicKey)
        val watchSubscriptionsParams = CoreNotifyParams.WatchSubscriptionsParams(didJwt.value)
        val request = NotifyRpc.NotifyWatchSubscriptions(params = watchSubscriptionsParams)
        val irnParams = IrnParams(Tags.NOTIFY_WATCH_SUBSCRIPTIONS, Ttl(thirtySeconds))

        jsonRpcInteractor.publishJsonRpcRequest(
            topic = requestTopic,
            params = irnParams,
            payload = request,
            envelopeType = EnvelopeType.ONE,
            participants = Participants(selfPublicKey, peerPublicKey),
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
