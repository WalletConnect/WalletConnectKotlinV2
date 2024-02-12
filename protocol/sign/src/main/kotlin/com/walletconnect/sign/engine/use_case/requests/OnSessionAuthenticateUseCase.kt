package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, authenticateSessionParams: SignParams.SessionAuthenticateParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE_RESPONSE, Ttl(dayInSeconds))
        logger.log("Received session authenticate: ${request.topic}")
        try {
            val expiry = authenticateSessionParams.authPayload.exp?.toLong()?.let { Expiry(it) }
            if (!CoreValidator.isExpiryWithinBounds(expiry)) {
                logger.log("Received session authenticate - expiry error: ${request.topic}")
                jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams)
                return@supervisorScope
            }

            //TODO: add eip155 validation
            println("kobe: received chains: ${authenticateSessionParams.authPayload.chains}")
            val url = authenticateSessionParams.requester.metadata.url
            resolveAttestationIdUseCase(request.id, request.message, url) { verifyContext ->
                scope.launch {
                    logger.log("Received session authenticate - emitting: ${request.topic}")
                    _events.emit(
                        EngineDO.SessionAuthenticateEvent(
                            request.id,
                            request.topic.value,
                            authenticateSessionParams.authPayload.toEngineDO(),
                            authenticateSessionParams.requester.toEngineDO(),
                            verifyContext.toEngineDO()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.log("Received session authenticate - cannot handle request: ${request.topic}")
            jsonRpcInteractor.respondWithError(request, Uncategorized.GenericError("Cannot handle a auth request: ${e.message}, topic: ${request.topic}"), irnParams)
            _events.emit(SDKError(e))
        }
    }
}