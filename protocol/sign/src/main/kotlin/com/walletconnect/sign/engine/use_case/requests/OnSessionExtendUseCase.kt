package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.mapper.toEngineDOSessionExtend
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionExtendUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, requestParams: SignParams.ExtendParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_EXTEND_RESPONSE, Ttl(dayInSeconds))
        logger.log("Session extend received on topic: ${request.topic}")
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                logger.error("Session extend received failure on topic: ${request.topic}")
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
                return@supervisorScope
            }

            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            val newExpiry = requestParams.expiry
            SignValidator.validateSessionExtend(newExpiry, session.expiry.seconds) { error ->
                logger.error("Session extend received failure on topic: ${request.topic} - invalid request: $error")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            sessionStorageRepository.extendSession(request.topic, newExpiry)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            logger.log("Session extend received on topic: ${request.topic} - emitting")
            _events.emit(session.toEngineDOSessionExtend(Expiry(newExpiry)))
        } catch (e: Exception) {
            logger.error("Session extend received failure on topic: ${request.topic}: $e")
            jsonRpcInteractor.respondWithError(request, Uncategorized.GenericError("Cannot update a session: ${e.message}, topic: ${request.topic}"), irnParams)
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }
}