package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingRequestsUseCaseByTopic(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : GetPendingRequestsUseCaseByTopicInterface {

    override suspend fun getPendingRequests(topic: Topic): List<PendingRequest<String>> = supervisorScope {
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .mapNotNull { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toPendingRequest(record) }
    }
}

internal interface GetPendingRequestsUseCaseByTopicInterface {
    suspend fun getPendingRequests(topic: Topic): List<PendingRequest<String>>
}