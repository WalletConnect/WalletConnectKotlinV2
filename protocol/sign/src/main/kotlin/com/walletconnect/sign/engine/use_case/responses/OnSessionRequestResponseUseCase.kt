package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.scope
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionRequestResponseUseCase {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(response: WCResponse, params: SignParams.SessionRequestParams) = supervisorScope {
        try {
            val result = when (response.response) {
                is JsonRpcResponse.JsonRpcResult -> (response.response as JsonRpcResponse.JsonRpcResult).toEngineDO()
                is JsonRpcResponse.JsonRpcError -> (response.response as JsonRpcResponse.JsonRpcError).toEngineDO()
            }
            val method = params.request.method
            _events.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result))
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }
}