package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.model.Direction
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionRequestResponseUseCase(
    private val logger: Logger,
    private val insertEventUseCase: InsertEventUseCase,
    private val clientId: String
) {

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(response: WCResponse, params: SignParams.SessionRequestParams) = supervisorScope {
        try {
            logger.log("Session request response received on topic: ${response.topic}")
            val result = when (response.response) {
                is JsonRpcResponse.JsonRpcResult -> (response.response as JsonRpcResponse.JsonRpcResult).toEngineDO()
                is JsonRpcResponse.JsonRpcError -> {
                    (response.response as JsonRpcResponse.JsonRpcError).toEngineDO()
                }
            }

            insertEventUseCase(
                Props(
                    EventType.SUCCESS,
                    Tags.SESSION_REQUEST_LINK_MODE_RESPONSE.id.toString(),
                    Properties(correlationId = response.response.id, clientId = clientId, direction = Direction.RECEIVED.state)
                )
            )
            val method = params.request.method
            logger.log("Session request response received on topic: ${response.topic} - emitting: $result")
            _events.emit(EngineDO.SessionPayloadResponse(response.topic.value, params.chainId, method, result))
        } catch (e: Exception) {
            logger.error("Session request response received failure: $e")
            _events.emit(SDKError(e))
        }
    }
}