package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


internal class SendPingUseCase(
    private val logger: Logger,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : SendPingUseCaseInterface {

    override fun ping(topic: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        val pingPayload = ChatRpc.ChatPing(params = ChatParams.PingParams(), topic = topic)
        val irnParams = IrnParams(Tags.CHAT_PING, Ttl(THIRTY_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, pingPayload,
            onSuccess = { pingSuccess(pingPayload, onSuccess, topic, onError) },
            onFailure = { error -> onError(error).also { logger.error("Ping sent error: $error") } })
    }

    private fun pingSuccess(
        pingPayload: ChatRpc.ChatPing,
        onSuccess: (String) -> Unit,
        topic: String,
        onError: (Throwable) -> Unit,
    ) {
        logger.log("Ping sent successfully")
        scope.launch {
            try {
                withTimeout(THIRTY_SECONDS_TIMEOUT) {
                    collectResponse(pingPayload.id) { result ->
                        cancel()
                        result.fold(
                            onSuccess = {
                                logger.log("Ping peer response success")
                                onSuccess(topic)
                            },
                            onFailure = { error ->
                                logger.log("Ping peer response error: $error")
                                onError(error)
                            })
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onError(e)
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

    private companion object {
        const val THIRTY_SECONDS_TIMEOUT: Long = 30000L
    }
}

internal interface SendPingUseCaseInterface {
    fun ping(topic: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)
}