package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.scope
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesSession
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.extractTimestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionUpdateResponseUseCase(
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) = supervisorScope {
        try {
            val sessionTopic = wcResponse.topic
            if (!sessionStorageRepository.isSessionValid(sessionTopic)) return@supervisorScope
            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(sessionTopic)
            if (!sessionStorageRepository.isUpdatedNamespaceResponseValid(session.topic.value, wcResponse.response.id.extractTimestamp())) {
                return@supervisorScope
            }

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session update namespaces response received")
                    val responseId = wcResponse.response.id
                    val namespaces = sessionStorageRepository.getTempNamespaces(responseId)
                    sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, namespaces, responseId)
                    sessionStorageRepository.markUnAckNamespaceAcknowledged(responseId)
                    _events.emit(EngineDO.SessionUpdateNamespacesResponse.Result(session.topic, session.sessionNamespaces.toMapOfEngineNamespacesSession()))
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.error("Peer failed to update session namespaces: ${response.error}")
                    _events.emit(EngineDO.SessionUpdateNamespacesResponse.Error(response.errorMessage))
                }
            }
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }
}