package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesSession
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.extractTimestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionUpdateUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: SignParams.UpdateNamespacesParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_UPDATE_RESPONSE, Ttl(dayInSeconds))
        logger.log("Session update received on topic: ${request.topic}")
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                logger.error("Session update received failure on topic: ${request.topic} - invalid session")
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
                return@supervisorScope
            }

            val session: SessionVO = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                logger.error("Session update received failure on topic: ${request.topic} - unauthorized peer")
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.UpdateRequest(Sequences.SESSION.name), irnParams)
                return@supervisorScope
            }

            SignValidator.validateSessionNamespace(params.namespaces, session.requiredNamespaces) { error ->
                logger.error("Session update received failure on topic: ${request.topic} - namespaces validation: $error")
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest(error.message), irnParams)
                return@supervisorScope
            }

            if (!sessionStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
                logger.error("Session update received failure on topic: ${request.topic} - Update Namespace Request ID too old")
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest("Update Namespace Request ID too old"), irnParams)
                return@supervisorScope
            }

            sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            logger.log("Session update received on topic: ${request.topic} - emitting")
            _events.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
        } catch (e: Exception) {
            logger.error("Session update received failure on topic: ${request.topic} - $e")
            jsonRpcInteractor.respondWithError(
                request,
                PeerError.Invalid.UpdateRequest("Updating Namespace Failed. Review Namespace structure. Error: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }
}