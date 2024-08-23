package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.pulse.model.Direction
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.android.verify.model.VerifyContext
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
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val pairingController: PairingControllerInterface,
    private val insertTelemetryEventUseCase: InsertTelemetryEventUseCase,
    private val insertEventUseCase: InsertEventUseCase,
    private val clientId: String,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, authenticateSessionParams: SignParams.SessionAuthenticateParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE_RESPONSE_AUTO_REJECT, Ttl(dayInSeconds))
        logger.log("Received session authenticate: ${request.topic}")
        try {
            if (Expiry(authenticateSessionParams.expiryTimestamp).isExpired()) {
                logger.log("Received session authenticate - expiry error: ${request.topic}")
                    .also { insertTelemetryEventUseCase(Props(type = EventType.Error.AUTHENTICATED_SESSION_EXPIRED, properties = Properties(topic = request.topic.value))) }
                jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams)
                _events.emit(SDKError(Throwable("Received session authenticate - expiry error: ${request.topic}")))
                return@supervisorScope
            }

            val url = authenticateSessionParams.metadataUrl
            pairingController.setRequestReceived(Core.Params.RequestReceived(request.topic.value))
            if (request.transportType == TransportType.LINK_MODE) {
                insertEventUseCase(
                    Props(
                        EventType.SUCCESS,
                        Tags.SESSION_AUTHENTICATE_LINK_MODE.id.toString(),
                        Properties(correlationId = request.id, clientId = clientId, direction = Direction.RECEIVED.state)
                    )
                )
            }
            resolveAttestationIdUseCase(request, url, linkMode = authenticateSessionParams.linkMode, appLink = authenticateSessionParams.appLink) { verifyContext ->
                emitSessionAuthenticate(request, authenticateSessionParams, verifyContext)
            }
        } catch (e: Exception) {
            logger.log("Received session authenticate - cannot handle request: ${request.topic}")
            jsonRpcInteractor.respondWithError(request, Uncategorized.GenericError("Cannot handle a auth request: ${e.message}, topic: ${request.topic}"), irnParams)
            _events.emit(SDKError(e))
        }
    }

    private fun emitSessionAuthenticate(
        request: WCRequest,
        authenticateSessionParams: SignParams.SessionAuthenticateParams,
        verifyContext: VerifyContext
    ) {
        scope.launch {
            logger.log("Received session authenticate - emitting: ${request.topic}")
            _events.emit(
                EngineDO.SessionAuthenticateEvent(
                    request.id,
                    request.topic.value,
                    authenticateSessionParams.authPayload.toEngineDO(),
                    authenticateSessionParams.requester.toEngineDO(),
                    authenticateSessionParams.expiryTimestamp,
                    verifyContext.toEngineDO()
                )
            )
        }
    }
}