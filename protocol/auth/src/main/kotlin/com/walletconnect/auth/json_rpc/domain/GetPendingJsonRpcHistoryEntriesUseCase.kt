package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.engine.mapper.toPendingRequest
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.toEntry

internal class GetPendingJsonRpcHistoryEntriesUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : GetPendingJsonRpcHistoryEntriesUseCaseInterface {

    override suspend fun getPendingRequests(): List<PendingRequest> {
        return jsonRpcHistory.getListOfPendingRecords()
            .filter { record -> record.method == JsonRpcMethod.WC_AUTH_REQUEST }
            .mapNotNull { record -> serializer.tryDeserialize<AuthRpc.AuthRequest>(record.body)?.params?.toEntry(record) }
            .map { jsonRpcHistoryEntry -> jsonRpcHistoryEntry.toPendingRequest() }
    }
}

internal interface GetPendingJsonRpcHistoryEntriesUseCaseInterface {
    suspend fun getPendingRequests(): List<PendingRequest>
}