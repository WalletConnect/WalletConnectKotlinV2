package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val THIRTY_SECONDS_TIMEOUT: Duration = 30.seconds

internal class PingUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) : PingUseCaseInterface {

    override suspend fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit, timeout: Duration) = supervisorScope {
        if (sessionStorageRepository.isSessionValid(Topic(topic))) {
            val pingPayload = SignRpc.SessionPing(params = SignParams.PingParams())
            val irnParams = IrnParams(Tags.SESSION_PING, Ttl(thirtySeconds))

            logger.log("Sending ping... topic: $topic")
            jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
                onSuccess = {
                    logger.log("Ping sent successfully, topic: $topic")
                    onPingSuccess(timeout, pingPayload, onSuccess, topic, onFailure)
                },
                onFailure = { error ->
                    logger.error("Ping sent error: $error, topic: $topic")
                    onFailure(error)
                })
        } else {
            onFailure(Throwable("Session topic is not valid"))
        }
    }

    private fun onPingSuccess(
        timeout: Duration,
        pingPayload: SignRpc.SessionPing,
        onSuccess: (String) -> Unit,
        topic: String,
        onFailure: (Throwable) -> Unit
    ) {
        scope.launch {
            try {
                withTimeout(timeout) {
                    collectResponse(pingPayload.id) { result ->
                        cancel()
                        result.fold(
                            onSuccess = {
                                logger.log("Ping peer response success")
                                onSuccess(topic)
                            },
                            onFailure = { error ->
                                logger.error("Ping peer response error: $error")
                                onFailure(error)
                            })
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onFailure(e)
            }
        }
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

internal interface PingUseCaseInterface {
    suspend fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit, timeout: Duration = THIRTY_SECONDS_TIMEOUT)
}