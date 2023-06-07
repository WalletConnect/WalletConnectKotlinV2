@file:JvmSynthetic

package com.walletconnect.push.wallet.engine.domain.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import com.walletconnect.push.common.model.EngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnPushRequestUseCase(
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : OnPushRequestUseCaseInterface {
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    override val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    override suspend operator fun invoke(request: WCRequest, params: PushParams.RequestParams) = supervisorScope {
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            subscriptionStorageRepository.insertSubscription(
                requestId = request.id,
                keyAgreementTopic = "",
                responseTopic = request.topic.value,
                peerPublicKeyAsHex = params.publicKey,
                subscriptionTopic = null,
                account = params.account,
                relayProtocol = null,
                relayData = null,
                name = params.metaData.name,
                description = params.metaData.description,
                url = params.metaData.url,
                icons = params.metaData.icons,
                native = params.metaData.redirect?.native,
                "",
                emptyMap(),
                calcExpiry()
            )

            val newSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(request.id)

            _engineEvent.emit(
                EngineDO.PushRequest(
                    newSubscription.requestId,
                    newSubscription.responseTopic.value,
                    newSubscription.account.value,
                    newSubscription.relay,
                    newSubscription.metadata
                )
            )
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )

            _engineEvent.emit(SDKError(e))
        }
    }
}

interface OnPushRequestUseCaseInterface {
    val engineEvent: SharedFlow<EngineEvent>

    suspend operator fun invoke(request: WCRequest, params: PushParams.RequestParams)
}