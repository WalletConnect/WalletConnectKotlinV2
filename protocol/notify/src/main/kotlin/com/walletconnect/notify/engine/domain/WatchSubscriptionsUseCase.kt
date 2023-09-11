@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import androidx.core.net.toUri
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyRpc
import kotlinx.coroutines.supervisorScope

internal class WatchSubscriptionsUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val keyManagementRepository: KeyManagementRepository,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
) {

    suspend operator fun invoke(accountId: AccountId, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val (peerPublicKey, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(NOTIFY_SERVER_URL.toUri()).getOrThrow()
        val requestTopic = keyManagementRepository.getTopicFromKey(peerPublicKey)
        val selfPublicKey = getOrGenerateAndStorePublicKey(requestTopic)
        val responseTopic = Topic(sha256(selfPublicKey.keyAsBytes))

        jsonRpcInteractor.subscribe(responseTopic) { error -> onFailure(error) }

        val didJwt = fetchDidJwtInteractor.watchSubscriptionsRequest(accountId, authenticationPublicKey).getOrElse { error -> return@supervisorScope onFailure(error) }
        val watchSubscriptionsParams = CoreNotifyParams.WatchSubscriptionsParams(didJwt.value)
        val request = NotifyRpc.NotifyWatchSubscriptions(params = watchSubscriptionsParams)
        val irnParams = IrnParams(Tags.NOTIFY_WATCH_SUBSCRIPTIONS, Ttl(THIRTY_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(requestTopic, irnParams, request, onSuccess = onSuccess, onFailure = onFailure)
    }


    private fun getOrGenerateAndStorePublicKey(requestTopic: Topic) = runCatching {
        keyManagementRepository.getPublicKey(requestTopic.getParticipantTag())
    }.getOrElse {
        keyManagementRepository.generateAndStoreX25519KeyPair().also { pubKey ->
            keyManagementRepository.setKey(pubKey, requestTopic.getParticipantTag())
        }
    }

    private companion object {
        const val NOTIFY_SERVER_URL = "https://dev.notify.walletconnect.com"
    }
}
