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
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.NotifyRpc
import kotlinx.coroutines.supervisorScope

internal class WatchSubscriptionsForEveryRegistereAccountUseCase(
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
    private val identitiesInteractor: IdentitiesInteractor,
    private val logger: Logger
) {

    suspend operator fun invoke(accountId: AccountId, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        logger.log("WatchSubscriptionsUseCase - peerPublicKey: $peerPublicKey")


    }


    private fun getOrGenerateAndStorePublicKey(requestTopic: Topic) = runCatching {
        keyManagementRepository.getPublicKey(requestTopic.getParticipantTag())
    }.getOrElse {
        keyManagementRepository.generateAndStoreX25519KeyPair().also { pubKey ->
            keyManagementRepository.setKey(pubKey, requestTopic.getParticipantTag())
        }
    }

    private companion object {
        const val NOTIFY_SERVER_URL = "https://dev.notify.walletconnect.com/"
    }
}
