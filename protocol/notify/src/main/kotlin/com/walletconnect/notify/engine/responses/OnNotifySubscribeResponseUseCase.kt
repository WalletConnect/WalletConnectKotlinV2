@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.notify.common.model.Error
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.jwt.subscription.SubscriptionResponseJwtClaim
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifySubscribeResponseUseCase {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.SubscribeParams) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {

                    val subscriptionResponseJwtClaim = extractVerifiedDidJwtClaims<SubscriptionResponseJwtClaim>((response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth)
                        .getOrElse { error -> return@supervisorScope _events.emit(SDKError(error)) }

                    _events.emit(
                        Subscription.Active(
                            AccountId(decodeDidPkh(subscriptionResponseJwtClaim.subject)),
                            emptyMap(),
                            Expiry(0L),
                            PublicKey(""),
                            Topic(""),
                            null,
                            null
                        )
                    )
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _events.emit(Error(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _events.emit(SDKError(exception))
        }
    }
}