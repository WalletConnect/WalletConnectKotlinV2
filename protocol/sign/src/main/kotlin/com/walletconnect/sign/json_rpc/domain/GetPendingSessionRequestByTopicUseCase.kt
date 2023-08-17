package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingSessionRequestByTopicUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) : GetPendingSessionRequestByTopicUseCaseInterface {

    override suspend fun getPendingSessionRequests(topic: Topic): List<EngineDO.SessionRequest> = supervisorScope {
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .mapNotNull { record ->
                serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toPendingRequest(record)
                    ?.toSessionRequest(metadataStorageRepository.getByTopicAndType(Topic(record.topic), AppMetaDataType.PEER))
            }
    }
}

internal interface GetPendingSessionRequestByTopicUseCaseInterface {
    suspend fun getPendingSessionRequests(topic: Topic): List<EngineDO.SessionRequest>
}