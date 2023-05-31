package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class RejectUseCase(
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
): RejectUseCaseInterface {

    override suspend fun reject(requestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithError(respondedSubscription.requestId, respondedSubscription.responseTopic, PeerError.Rejected.UserRejected(reason), irnParams) { error ->
                return@respondWithError onFailure(error)
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface RejectUseCaseInterface {
    suspend fun reject(requestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}