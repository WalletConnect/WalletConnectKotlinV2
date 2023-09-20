@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

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
import com.walletconnect.notify.common.NotifyServerUrl
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
    private val notifyServerUrl: NotifyServerUrl,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.SubscriptionsChangedParams) = supervisorScope {
        val jwtClaims = extractVerifiedDidJwtClaims<SubscriptionsChangedRequestJwtClaim>(params.subscriptionsChangedAuth).getOrElse { error -> return@supervisorScope logger.error(error) }

        /* TODO: Add caching of Notify Server keyAgreement and authentication keys
        *     */
        val (_, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrThrow()

        /* TODO: Add validation after ETHNY
        *   jwtClaims.iat - compare with current time. Has to be lower
        *   jwtClaims.exp - compare with current time. Has to be higher
        *   jwtClaims.act == "notify_subscriptions_changed"
        *   jwtClaims.iss - did:key of Notify Server authentication key. Add logic when cached value does not match jwtClaims.iss then fetch value again and if value still does not match then throw
        *   jwtClaims.aud - did:key of client identity key. Client must have this identity key */

        val account = decodeDidPkh(jwtClaims.subject)
        val subscriptions = setActiveSubscriptionsUseCase(account, jwtClaims.subscriptions)

        val didJwt = fetchDidJwtInteractor.subscriptionsChangedResponse(AccountId(account), authenticationPublicKey).getOrElse { error -> return@supervisorScope logger.error(error) }

        val responseParams = ChatNotifyResponseAuthParams.ResponseAuth(didJwt.value)
        val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIPTIONS_CHANGED_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.respondWithParams(request.id, request.topic, responseParams, irnParams) { error -> logger.error(error) }

        launch { _events.emit(SubscriptionChanged(subscriptions)) }
    }

}