package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.engine.sessionRequestEventsQueue
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RespondSessionRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val logger: Logger,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
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
            return@supervisorScope onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        handleExpiration(jsonRpcResponse, topic)

        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        jsonRpcInteractor.publishJsonRpcResponse(topic = Topic(topic), params = irnParams, response = jsonRpcResponse,
            onSuccess = {
                logger.log("Session payload sent successfully")
                scope.launch {
                    supervisorScope {
                        removePendingSessionRequestAndEmit(jsonRpcResponse)
                    }
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session payload response error: $error")
                onFailure(error)
            }
        )
    }

    private suspend fun handleExpiration(jsonRpcResponse: JsonRpcResponse, topic: String) {
        getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id)?.params?.request?.expiry?.let { expiry ->
            if (!CoreValidator.isExpiryWithinBounds(expiry)) {
                val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
                val request = WCRequest(Topic(topic), jsonRpcResponse.id, JsonRpcMethod.WC_SESSION_REQUEST, object : ClientParams {})
                jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams, onSuccess = {
                    scope.launch {
                        supervisorScope {
                            removePendingSessionRequestAndEmit(jsonRpcResponse)
                        }
                    }
                })
                throw InvalidExpiryException()
            }
        }
    }

    private suspend fun removePendingSessionRequestAndEmit(jsonRpcResponse: JsonRpcResponse) {
        verifyContextStorageRepository.delete(jsonRpcResponse.id)
        sessionRequestEventsQueue.find { pendingRequestEvent -> pendingRequestEvent.request.request.id == jsonRpcResponse.id }?.let { event ->
            sessionRequestEventsQueue.remove(event)
        }
        if (sessionRequestEventsQueue.isNotEmpty()) {
            sessionRequestEventsQueue.find { event -> CoreValidator.isExpiryWithinBounds(event.request.expiry) }?.let { event ->
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