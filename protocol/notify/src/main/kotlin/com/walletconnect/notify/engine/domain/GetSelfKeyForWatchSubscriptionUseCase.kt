@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.utils.getPeerTag
import com.walletconnect.foundation.common.model.Topic

internal class GetSelfKeyForWatchSubscriptionUseCase(
    private val keyManagementRepository: KeyManagementRepository,
) {
    suspend operator fun invoke(requestTopic: Topic, accountId: AccountId) = runCatching {
        keyManagementRepository.getPublicKey(Pair(accountId, requestTopic).getPeerTag())
    }.getOrElse {
        keyManagementRepository.generateAndStoreX25519KeyPair().also { pubKey ->
            keyManagementRepository.setKey(pubKey, Pair(accountId, requestTopic).getPeerTag())
        }
    }
}
