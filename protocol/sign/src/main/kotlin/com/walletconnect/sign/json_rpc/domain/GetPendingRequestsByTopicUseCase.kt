package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toSessionRequest
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequest

internal class GetPendingRequestsUseCaseByTopic(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : GetPendingRequestsUseCaseByTopicInterface {

    override fun getPendingRequests(topic: Topic): List<PendingRequest<String>> =
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body) != null }
            .map { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)!!.toPendingRequest(record) }
}

internal interface GetPendingRequestsUseCaseByTopicInterface {
    fun getPendingRequests(topic: Topic): List<PendingRequest<String>>
}