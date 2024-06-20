package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.RequestExpiredException
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.engine.sessionRequestEventsQueue
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RespondSessionRequestUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface,
    private val logger: Logger,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) : RespondSessionRequestUseCaseInterface {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    override val events: SharedFlow<EngineEvent> = _events.asSharedFlow()
    override suspend fun respondSessionRequest(
        topic: String,
        jsonRpcResponse: JsonRpcResponse,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val topicWrapper = Topic(topic)
        if (!sessionStorageRepository.isSessionValid(topicWrapper)) {
            logger.error("Request response -  invalid session: $topic, id: ${jsonRpcResponse.id}")
            return@supervisorScope onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }
        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(topicWrapper)
            .run {
                val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                this.copy(peerAppMetaData = peerAppMetaData)
            }

        if (getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) == null) {
            logger.error("Request doesn't exist: $topic, id: ${jsonRpcResponse.id}")
            throw RequestExpiredException("This request has expired, id: ${jsonRpcResponse.id}")
        }
        getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id)?.params?.request?.expiryTimestamp?.let {
            if (Expiry(it).isExpired()) {
                logger.error("Request Expired: $topic, id: ${jsonRpcResponse.id}")
                throw RequestExpiredException("This request has expired, id: ${jsonRpcResponse.id}")
            }
        }
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(fiveMinutesInSeconds))
        logger.log("Sending session request response on topic: $topic, id: ${jsonRpcResponse.id}")

        if (session.transportType == TransportType.LINK_MODE && session.linkMode == true) {
            if (session.appLink.isNullOrEmpty()) return@supervisorScope onFailure(IllegalStateException("App link is missing"))
            try {
                removePendingSessionRequestAndEmit(jsonRpcResponse.id)
                linkModeJsonRpcInteractor.triggerResponse(Topic(topic), jsonRpcResponse, session.appLink)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        } else {
            jsonRpcInteractor.publishJsonRpcResponse(topic = Topic(topic), params = irnParams, response = jsonRpcResponse,
                onSuccess = {
                    onSuccess()
                    logger.log("Session request response sent successfully on topic: $topic, id: ${jsonRpcResponse.id}")
                    scope.launch {
                        supervisorScope {
                            removePendingSessionRequestAndEmit(jsonRpcResponse.id)
                        }
                    }
                },
                onFailure = { error ->
                    logger.error("Sending session response error: $error, id: ${jsonRpcResponse.id}")
                    onFailure(error)
                }
            )
        }
    }

    private suspend fun removePendingSessionRequestAndEmit(id: Long) {
        verifyContextStorageRepository.delete(id)
        sessionRequestEventsQueue.find { pendingRequestEvent -> pendingRequestEvent.request.request.id == id }?.let { event ->
            sessionRequestEventsQueue.remove(event)
        }
        if (sessionRequestEventsQueue.isNotEmpty()) {
            sessionRequestEventsQueue.find { event -> if (event.request.expiry != null) !event.request.expiry.isExpired() else true }?.let { event ->
                _events.emit(event)
            }
        }
    }
}

internal interface RespondSessionRequestUseCaseInterface {
    val events: SharedFlow<EngineEvent>
    suspend fun respondSessionRequest(
        topic: String,
        jsonRpcResponse: JsonRpcResponse,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}