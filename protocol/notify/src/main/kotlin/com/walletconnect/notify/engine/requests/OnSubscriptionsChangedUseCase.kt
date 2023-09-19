@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import androidx.core.net.toUri
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.notify.common.model.SubscriptionChanged
import com.walletconnect.notify.data.jwt.subscriptionsChanged.SubscriptionsChangedRequestJwtClaim
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSubscriptionsChangedUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.SubscriptionsChangedParams) = supervisorScope {
        val jwtClaims = extractVerifiedDidJwtClaims<SubscriptionsChangedRequestJwtClaim>(params.subscriptionsChangedAuth).getOrElse { error -> return@supervisorScope logger.error(error) }

        val account = decodeDidPkh(jwtClaims.subject)
        val subscriptions = setActiveSubscriptionsUseCase(account, jwtClaims.subscriptions)
        logger.log("OnSubscriptionsChangedUseCase - subscriptions: ${subscriptions.size}")
        logger.log("OnSubscriptionsChangedUseCase - types: ${subscriptions.getOrNull(0)?.mapOfNotificationScope?.filter { it.value.isSelected }?.map { it.key }}")


        //todo optimise fetching notify server auth key
        val (_, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(NOTIFY_SERVER_URL.toUri()).getOrThrow()

        val didJwt = fetchDidJwtInteractor.subscriptionsChangedResponse(AccountId(account), authenticationPublicKey).getOrElse { error -> return@supervisorScope logger.error(error) }

        val responseParams = ChatNotifyResponseAuthParams.ResponseAuth(didJwt.value)
        val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIPTIONS_CHANGED_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.respondWithParams(request.id, request.topic, responseParams, irnParams) { error -> logger.error(error) }

        launch { _events.emit(SubscriptionChanged(subscriptions)) }
    }

    private companion object {
        const val NOTIFY_SERVER_URL = "https://notify.walletconnect.com/"
    }
}