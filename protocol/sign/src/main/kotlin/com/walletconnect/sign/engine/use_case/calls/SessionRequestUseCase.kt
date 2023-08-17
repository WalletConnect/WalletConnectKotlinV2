package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidRequestException
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.exceptions.UnauthorizedMethodException
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.Date
import java.util.concurrent.TimeUnit

internal class SessionRequestUseCase(
    private val sessionStorageRepository: SessionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val logger: Logger,
) : SessionRequestUseCaseInterface {
    private val _errors: MutableSharedFlow<SDKError> = MutableSharedFlow()
    override val errors: SharedFlow<SDKError> = _errors.asSharedFlow()

    override suspend fun sessionRequest(request: EngineDO.Request, onSuccess: (Long) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        if (!sessionStorageRepository.isSessionValid(Topic(request.topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE${request.topic}")
        }

        val nowInSeconds = TimeUnit.SECONDS.convert(Date().time, TimeUnit.SECONDS)
        if (!CoreValidator.isExpiryWithinBounds(request.expiry ?: Expiry(300))) {
            throw InvalidExpiryException()
        }

        SignValidator.validateSessionRequest(request) { error ->
            throw InvalidRequestException(error.message)
        }

        val namespaces: Map<String, NamespaceVO.Session> =
            sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(request.topic)).sessionNamespaces
        SignValidator.validateChainIdWithMethodAuthorisation(request.chainId, request.method, namespaces) { error ->
            throw UnauthorizedMethodException(error.message)
        }

        val params = SignParams.SessionRequestParams(SessionRequestVO(request.method, request.params), request.chainId)
        val sessionPayload = SignRpc.SessionRequest(params = params)
        val irnParamsTtl = request.expiry?.run {
            val defaultTtl = FIVE_MINUTES_IN_SECONDS
            val extractedTtl = seconds - nowInSeconds
            val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl

            Ttl(newTtl)
        } ?: Ttl(FIVE_MINUTES_IN_SECONDS)
        val irnParams = IrnParams(Tags.SESSION_REQUEST, irnParamsTtl, true)
        val requestTtlInSeconds = request.expiry?.run { seconds - nowInSeconds } ?: FIVE_MINUTES_IN_SECONDS

        jsonRpcInteractor.publishJsonRpcRequest(Topic(request.topic), irnParams, sessionPayload,
            onSuccess = {
                logger.log("Session request sent successfully")
                onSuccess(sessionPayload.id)
                scope.launch {
                    try {
                        withTimeout(TimeUnit.SECONDS.toMillis(requestTtlInSeconds)) {
                            collectResponse(sessionPayload.id) { cancel() }
                        }
                    } catch (e: TimeoutCancellationException) {
                        _errors.emit(SDKError(e))
                    }
                }
            },
            onFailure = { error ->
                logger.error("Sending session request error: $error")
                onFailure(error)
            }
        )
    }

    private suspend fun collectResponse(id: Long, onResponse: (Result<JsonRpcResponse.JsonRpcResult>) -> Unit = {}) {
        jsonRpcInteractor.peerResponse
            .filter { response -> response.response.id == id }
            .collect { response ->
                when (val result = response.response) {
                    is JsonRpcResponse.JsonRpcResult -> onResponse(Result.success(result))
                    is JsonRpcResponse.JsonRpcError -> onResponse(Result.failure(Throwable(result.errorMessage)))
                }
            }
    }
}

internal interface SessionRequestUseCaseInterface {
    val errors: SharedFlow<SDKError>
    suspend fun sessionRequest(request: EngineDO.Request, onSuccess: (Long) -> Unit, onFailure: (Throwable) -> Unit)
}