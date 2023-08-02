@file:JvmSynthetic

package com.walletconnect.notify.engine.responses;

import android.content.res.Resources
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent;
import com.walletconnect.notify.common.calcExpiry
import com.walletconnect.notify.data.jwt.NotifySubscriptionJwtClaim
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.common.model.toDb
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyUpdateResponseUseCase(
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, updateParams: NotifyParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(wcResponse.topic.value)
                        ?: throw Resources.NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val notifyUpdateJwtClaim = extractVerifiedDidJwtClaims<NotifySubscriptionJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _events.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = notifyUpdateJwtClaim.scope.split(" ")
                    val updateScopeMap: Map<String, EngineDO.Scope.Cached> = subscription.mapOfScope.entries.associate { (scopeName, scopeDescIsSelected) ->
                        val (desc, _) = scopeDescIsSelected
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to EngineDO.Scope.Cached(scopeName, desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionRepository.updateSubscriptionScopeAndJwtByNotifyTopic(
                        subscription.notifyTopic.value,
                        updateScopeMap.toDb(),
                        newExpiry.seconds
                    )

                    with(subscription) { EngineDO.Update.Result(account, mapOfScope, expiry, dappGeneratedPublicKey, notifyTopic, dappMetaData, relay) }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    EngineDO.Update.Error(wcResponse.response.id, response.error.message)
                }
            }
        } catch (exception: Exception) {
            SDKError(exception)
        }

        _events.emit(resultEvent)
    }
}