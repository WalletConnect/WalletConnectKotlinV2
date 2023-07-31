package com.walletconnect.push.engine.responses;

import android.content.res.Resources
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent;
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.jwt.PushSubscriptionJwtClaim
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toDb
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnPushUpdateResponseUseCase(
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, updateParams: PushParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val subscription = subscriptionRepository.getActiveSubscriptionByPushTopic(wcResponse.topic.value)
                        ?: throw Resources.NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")
                    val pushUpdateJwtClaim = extractVerifiedDidJwtClaims<PushSubscriptionJwtClaim>(updateParams.subscriptionAuth).getOrElse { error ->
                        _events.emit(SDKError(error))
                        return@supervisorScope
                    }
                    val listOfUpdateScopeNames = pushUpdateJwtClaim.scope.split(" ")
                    val updateScopeMap: Map<String, EngineDO.PushScope.Cached> = subscription.mapOfScope.entries.associate { (scopeName, scopeDescIsSelected) ->
                        val (desc, _) = scopeDescIsSelected
                        val isNewScopeTrue = listOfUpdateScopeNames.contains(scopeName)

                        scopeName to EngineDO.PushScope.Cached(scopeName, desc, isNewScopeTrue)
                    }
                    val newExpiry = calcExpiry()

                    subscriptionRepository.updateSubscriptionScopeAndJwtByPushTopic(
                        subscription.pushTopic.value,
                        updateScopeMap.toDb(),
                        newExpiry.seconds
                    )

                    with(subscription) { EngineDO.PushUpdate.Result(account, mapOfScope, expiry, dappGeneratedPublicKey, pushTopic, dappMetaData, relay) }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    EngineDO.PushUpdate.Error(wcResponse.response.id, response.error.message)
                }
            }
        } catch (exception: Exception) {
            SDKError(exception)
        }

        _events.emit(resultEvent)
    }
}