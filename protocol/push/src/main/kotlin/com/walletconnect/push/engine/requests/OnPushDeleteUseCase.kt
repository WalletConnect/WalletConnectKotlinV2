package com.walletconnect.push.engine.requests

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnPushDeleteUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest) = supervisorScope {
        logger.error("onPushDelete: $request")
        val irnParams = IrnParams(Tags.PUSH_DELETE_RESPONSE, Ttl(DAY_IN_SECONDS))

        val result = try {
            val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(request.topic.value)

            if (subscription == null) {
                SDKError(IllegalStateException("Cannot find subscription for topic: ${request.topic}"))
            } else {
                jsonRpcInteractor.respondWithSuccess(request, irnParams)
                jsonRpcInteractor.unsubscribe(subscription.pushTopic)
                subscriptionRepository.deleteSubscriptionByPushTopic(subscription.pushTopic.value)

                EngineDO.PushDelete(request.topic.value)
            }
        } catch (e: Exception) {
            SDKError(e)
        }

        _events.emit(result)
    }
}