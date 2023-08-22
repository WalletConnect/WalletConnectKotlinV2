@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.DeleteSubscription
import com.walletconnect.notify.data.jwt.delete.DeleteRequestJwtClaim
import com.walletconnect.notify.data.storage.SubscriptionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyDeleteUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, requestParams: CoreNotifyParams.DeleteParams) = supervisorScope {
        logger.error("onNotifyDelete: $request")
        val irnParams = IrnParams(Tags.NOTIFY_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))

        val result = extractVerifiedDidJwtClaims<DeleteRequestJwtClaim>(requestParams.deleteAuth).mapCatching { _ ->
            val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(request.topic.value)

            if (subscription == null) {
                SDKError(IllegalStateException("Cannot find subscription for topic: ${request.topic}"))
            } else {
                jsonRpcInteractor.respondWithSuccess(request, irnParams)
                jsonRpcInteractor.unsubscribe(subscription.notifyTopic)
                subscriptionRepository.deleteSubscriptionByNotifyTopic(subscription.notifyTopic.value)
                metadataStorageRepository.deleteMetaData(subscription.notifyTopic)

                DeleteSubscription(request.topic.value)
            }
        }.getOrElse {
            SDKError(it)
        }

        _events.emit(result)
    }
}