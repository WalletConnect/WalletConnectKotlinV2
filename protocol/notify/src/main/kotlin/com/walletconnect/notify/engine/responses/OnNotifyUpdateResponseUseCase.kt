@file:JvmSynthetic

package com.walletconnect.notify.engine.responses;

import android.content.res.Resources
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.notify.common.calcExpiry
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.UpdateSubscription
import com.walletconnect.notify.common.model.toDb
import com.walletconnect.notify.data.jwt.subscription.SubscriptionRequestJwtClaim
import com.walletconnect.notify.data.storage.SubscriptionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyUpdateResponseUseCase(
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, updateParams: CoreNotifyParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(wcResponse.topic.value)
                        ?: throw Resources.NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val notifyUpdateJwtClaim = extractVerifiedDidJwtClaims<SubscriptionRequestJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _events.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = notifyUpdateJwtClaim.scope.split(" ")
                    val updateNotificationScopeMap: Map<String, NotificationScope.Cached> = subscription.mapOfNotificationScope.entries.associate { (scopeName, scopeDescIsSelected) ->
                        val (desc, _) = scopeDescIsSelected
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to NotificationScope.Cached(scopeName, desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionRepository.updateSubscriptionScopeAndJwtByNotifyTopic(
                        subscription.notifyTopic.value,
                        updateNotificationScopeMap.toDb(),
                        newExpiry.seconds
                    )

                    with(subscription) { UpdateSubscription.Result(account, mapOfNotificationScope, expiry, dappGeneratedPublicKey, notifyTopic, dappMetaData, relay) }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    UpdateSubscription.Error(wcResponse.response.id, response.error.message)
                }
            }
        } catch (exception: Exception) {
            SDKError(exception)
        }

        _events.emit(resultEvent)
    }
}