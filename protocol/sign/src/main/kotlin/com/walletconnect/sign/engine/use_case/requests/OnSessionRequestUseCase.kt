package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.sessionRequestsQueue
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnSessionRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    operator fun invoke(request: WCRequest, params: SignParams.SessionRequestParams) {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        try {
            if (!CoreValidator.isExpiryWithinBounds(params.request.expiry)) {
                jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams)
                return
            }

            SignValidator.validateSessionRequest(params.toEngineDO(request.topic)) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return
            }
            val (sessionNamespaces: Map<String, NamespaceVO.Session>, sessionPeerAppMetaData: AppMetaData?) =
                sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
                    .run {
                        val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                        this.sessionNamespaces to peerAppMetaData
                    }

            val method = params.request.method
            SignValidator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            val url = sessionPeerAppMetaData?.url ?: String.Empty
            resolveAttestationIdUseCase(request.id, request.message, url) { verifyContext ->
                val sessionRequestEvent = EngineDO.SessionRequestEvent(params.toEngineDO(request, sessionPeerAppMetaData), verifyContext.toEngineDO())
                val event = if (sessionRequestsQueue.isEmpty()) {
                    sessionRequestEvent
                } else {
                    sessionRequestsQueue.first()
                }

                sessionRequestsQueue.addLast(sessionRequestEvent)
                scope.launch { _events.emit(event) }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _events.emit(SDKError(e)) }
            return
        }
    }
}