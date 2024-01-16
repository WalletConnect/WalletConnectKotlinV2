@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.notify.common.model.CreateSubscription
import com.walletconnect.notify.data.jwt.subscription.SubscriptionRequestJwtClaim
import com.walletconnect.notify.data.jwt.subscription.SubscriptionResponseJwtClaim
import com.walletconnect.notify.engine.domain.FindRequestedSubscriptionUseCase
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifySubscribeResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val findRequestedSubscriptionUseCase: FindRequestedSubscriptionUseCase,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

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

                    CreateSubscription.Result(subscription)
                }

                is JsonRpcResponse.JsonRpcError -> CreateSubscription.Error(wcResponse.response.id, response.error.message)

            }
        } catch (exception: Exception) {
            SDKError(exception)
        }

        _events.emit(resultEvent)
    }
}