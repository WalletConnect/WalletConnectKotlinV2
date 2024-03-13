package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingSessionRequests(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    suspend operator fun invoke(): List<Request<String>> = supervisorScope {
        jsonRpcHistory.getListOfPendingRecords()
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .mapNotNull { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toRequest(record) }
    }
}