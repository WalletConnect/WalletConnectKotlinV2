@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.NotifyServerUrl
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import kotlinx.coroutines.supervisorScope
import timber.log.Timber

internal class WatchSubscriptionsUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val keyManagementRepository: KeyManagementRepository,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val notifyServerUrl: NotifyServerUrl,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
) {

    suspend operator fun invoke(accountId: AccountId, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val (peerPublicKey, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrElse { useCaseError -> return@supervisorScope onFailure(useCaseError) }

        val requestTopic = keyManagementRepository.getTopicFromKey(peerPublicKey)

        val selfPublicKey = getOrGenerateAndStorePublicKey(requestTopic)
        val responseTopic = keyManagementRepository.generateTopicFromKeyAgreement(selfPublicKey, peerPublicKey)

        jsonRpcInteractor.subscribe(responseTopic) { error -> onFailure(error) }

        val account = registeredAccountsRepository.getAccountByAccountId(accountId.value)
        val didJwt = fetchDidJwtInteractor.watchSubscriptionsRequest(accountId, authenticationPublicKey, if(account.isLimited) account.appDomain else null)
            .getOrElse { error -> return@supervisorScope onFailure(error) }
        Timber.d(didJwt.value)
        val watchSubscriptionsParams = CoreNotifyParams.WatchSubscriptionsParams(didJwt.value)
        val request = NotifyRpc.NotifyWatchSubscriptions(params = watchSubscriptionsParams)
        val irnParams = IrnParams(Tags.NOTIFY_WATCH_SUBSCRIPTIONS, Ttl(THIRTY_SECONDS))

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


    private fun getOrGenerateAndStorePublicKey(requestTopic: Topic) = runCatching {
        keyManagementRepository.getPublicKey(requestTopic.getParticipantTag())
    }.getOrElse {
        keyManagementRepository.generateAndStoreX25519KeyPair().also { pubKey ->
            keyManagementRepository.setKey(pubKey, requestTopic.getParticipantTag())
        }
    }
}
