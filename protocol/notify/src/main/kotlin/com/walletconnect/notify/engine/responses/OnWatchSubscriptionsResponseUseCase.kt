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
import com.walletconnect.notify.common.model.SubscriptionChanged
import com.walletconnect.notify.data.jwt.watchSubscriptions.WatchSubscriptionsResponseJwtClaim
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnWatchSubscriptionsResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, watchSubscriptionsParams: CoreNotifyParams.WatchSubscriptionsParams) = supervisorScope {

        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val jwtClaims = extractVerifiedDidJwtClaims<WatchSubscriptionsResponseJwtClaim>(responseAuth).getOrThrow()
                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(jwtClaims.subject), jwtClaims.subscriptions)

                    SubscriptionChanged(subscriptions)
                }

                is JsonRpcResponse.JsonRpcError -> {
                    SDKError(Exception(response.errorMessage))
                }
            }
        } catch (exception: Exception) {
            SDKError(exception)
        }

        _events.emit(resultEvent)
    }
}