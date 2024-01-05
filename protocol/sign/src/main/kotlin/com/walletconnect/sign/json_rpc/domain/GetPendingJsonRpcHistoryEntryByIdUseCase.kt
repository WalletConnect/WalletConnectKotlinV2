package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.model.toRequest

internal class GetPendingJsonRpcHistoryEntryByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): Request<SignParams.SessionRequestParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: Request<SignParams.SessionRequestParams>? = null

        if (record != null) {
            val sessionRequest: SignRpc.SessionRequest? = serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)
            if (sessionRequest != null) {
                entry = record.toRequest(sessionRequest.params)
            }
        }

        return entry
    }
}