@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.notify.common.model.CreateSubscription
import com.walletconnect.notify.data.jwt.subscription.SubscriptionRequestJwtClaim
import com.walletconnect.notify.data.jwt.subscription.SubscriptionResponseJwtClaim
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.FindRequestedSubscriptionUseCase
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifySubscribeResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val findRequestedSubscriptionUseCase: FindRequestedSubscriptionUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.SubscribeParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.SubscribeParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.SubscribeParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<SubscriptionResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()
                    val requestJwtClaim = extractVerifiedDidJwtClaims<SubscriptionRequestJwtClaim>(params.subscriptionAuth).getOrThrow()
                    val subscription = findRequestedSubscriptionUseCase(requestJwtClaim.audience, subscriptions)

                    CreateSubscription.Success(subscription)
                }

                is JsonRpcResponse.JsonRpcError -> {
                    removeOptimisticallyAddedSubscription(wcResponse.topic)
                    CreateSubscription.Error(Throwable(response.error.message))
                }

            }
        } catch (e: Exception) {
            removeOptimisticallyAddedSubscription(wcResponse.topic)
            logger.error(e)
            SDKError(e)
        }

        _events.emit(params to resultEvent)
    }


    private suspend fun removeOptimisticallyAddedSubscription(topic: Topic) =
        runCatching { subscriptionRepository.deleteSubscriptionByNotifyTopic(topic.value) }.getOrElse { logger.error("OnSubscribeResponse - Error - No subscription found for removal") }
}