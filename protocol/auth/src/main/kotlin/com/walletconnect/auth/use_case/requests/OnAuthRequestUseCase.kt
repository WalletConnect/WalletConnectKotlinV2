package com.walletconnect.auth.use_case.requests

import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.model.Events
import com.walletconnect.foundation.common.model.Ttl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnAuthRequestUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val pairingController: PairingControllerInterface,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcRequest: WCRequest, authParams: AuthParams.RequestParams) = supervisorScope {
        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(dayInSeconds))
        try {
            authParams.expiry?.let {
                if (it.isExpired()) {
                    jsonRpcInteractor.respondWithError(wcRequest, Invalid.RequestExpired, irnParams)
                    return@supervisorScope
                }
            }

            val url = authParams.requester.metadata.url
            resolveAttestationIdUseCase(wcRequest, url) { verifyContext ->
                scope.launch { _events.emit(Events.OnAuthRequest(wcRequest.id, wcRequest.topic.value, authParams.payloadParams, verifyContext)) }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(wcRequest, Uncategorized.GenericError("Cannot handle a auth request: ${e.message}, topic: ${wcRequest.topic}"), irnParams)
            _events.emit(SDKError(e))
        }
    }
}