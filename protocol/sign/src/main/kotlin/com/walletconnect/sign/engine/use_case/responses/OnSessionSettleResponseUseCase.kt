package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionSettleResponseUseCase(
    private val sessionStorageRepository: SessionStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val crypto: KeyManagementRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) = supervisorScope {
        try {
            val sessionTopic = wcResponse.topic
            if (!sessionStorageRepository.isSessionValid(sessionTopic)) return@supervisorScope
            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(sessionTopic).run {
                val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                this.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = peerAppMetaData)
            }

            when (wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session settle success received")
                    sessionStorageRepository.acknowledgeSession(sessionTopic)
                    _events.emit(EngineDO.SettledSessionResponse.Result(session.toEngineDO()))
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.error("Peer failed to settle session: ${(wcResponse.response as JsonRpcResponse.JsonRpcError).errorMessage}")
                    jsonRpcInteractor.unsubscribe(sessionTopic, onSuccess = {
                        sessionStorageRepository.deleteSession(sessionTopic)
                        crypto.removeKeys(sessionTopic.value)
                    })
                }
            }
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }
}