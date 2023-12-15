package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.model.toPending

class GetPendingSessionAuthenticateRequest(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    internal operator fun invoke(id: Long): PendingRequest<SignParams.SessionAuthenticateParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: PendingRequest<SignParams.SessionAuthenticateParams>? = null

        if (record != null) {
            val authRequest: SignRpc.SessionAuthenticate? = serializer.tryDeserialize<SignRpc.SessionAuthenticate>(record.body)
            if (authRequest != null) {
                entry = record.toPending(authRequest.params)
            }
        }

        return entry
    }
}