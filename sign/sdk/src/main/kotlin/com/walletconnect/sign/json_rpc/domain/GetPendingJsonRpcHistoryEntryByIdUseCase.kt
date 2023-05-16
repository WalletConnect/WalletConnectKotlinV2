package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.model.toPending

internal class GetPendingJsonRpcHistoryEntryByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): PendingRequest<SignParams.SessionRequestParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: PendingRequest<SignParams.SessionRequestParams>? = null

        if (record != null) {
            val sessionRequest: SignRpc.SessionRequest? = serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)
            if (sessionRequest != null) {
                entry = record.toPending(sessionRequest.params)
            }
        }

        return entry
    }
}