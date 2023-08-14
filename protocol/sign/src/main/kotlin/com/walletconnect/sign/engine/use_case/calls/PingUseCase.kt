package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.android.pairing.client.PairingInterface
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
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val THIRTY_SECONDS_TIMEOUT: Duration = 30.seconds

internal class PingUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val pairingInterface: PairingInterface,
    private val logger: Logger
) : PingUseCaseInterface {

    override fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit, timeout: Duration) {
        if (sessionStorageRepository.isSessionValid(Topic(topic))) {
            val pingPayload = SignRpc.SessionPing(params = SignParams.PingParams())
            val irnParams = IrnParams(Tags.SESSION_PING, Ttl(THIRTY_SECONDS))

            jsonRpcInteractor.publishJsonRpcRequest(
                Topic(topic), irnParams, pingPayload,
                onSuccess = {
                    logger.log("Ping sent successfully")
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
                                            logger.log("Ping peer response error: $error")
                                            onFailure(error)
                                        })
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            onFailure(e)
                        }
                    }
                },
                onFailure = { error ->
                    logger.log("Ping sent error: $error")
                    onFailure(error)
                })
        } else {
            pairingInterface.ping(Core.Params.Ping(topic), object : Core.Listeners.PairingPing {
                override fun onSuccess(pingSuccess: Core.Model.Ping.Success) {
                    onSuccess(pingSuccess.topic)
                }

                override fun onError(pingError: Core.Model.Ping.Error) {
                    onFailure(pingError.error)
                }
            })
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
    fun ping(topic: String, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit, timeout: Duration = THIRTY_SECONDS_TIMEOUT)
}