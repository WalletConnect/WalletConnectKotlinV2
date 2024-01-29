@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.notify.common.model.CreateSubscription
import com.walletconnect.notify.common.model.UpdateSubscription
import com.walletconnect.notify.data.jwt.update.UpdateRequestJwtClaim
import com.walletconnect.notify.data.jwt.update.UpdateResponseJwtClaim
import com.walletconnect.notify.engine.domain.FindRequestedSubscriptionUseCase
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnUpdateResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val findRequestedSubscriptionUseCase: FindRequestedSubscriptionUseCase,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.UpdateParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.UpdateParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<UpdateResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()
                    val requestJwtClaim = extractVerifiedDidJwtClaims<UpdateRequestJwtClaim>(params.updateAuth).getOrThrow()
                    val subscription = findRequestedSubscriptionUseCase(requestJwtClaim.audience, subscriptions)

                    UpdateSubscription.Success(subscription)
                }

                is JsonRpcResponse.JsonRpcError -> UpdateSubscription.Error(Throwable(response.error.message))
            }
        } catch (e: Exception) {
            logger.error(e)
            CreateSubscription.Error(e)
        }

        _events.emit(params to resultEvent)
    }
}