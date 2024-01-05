package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.model.toRequest

class GetSessionAuthenticateRequest(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {
    internal operator fun invoke(id: Long): Request<SignParams.SessionAuthenticateParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getRecordById(id)
        var entry: Request<SignParams.SessionAuthenticateParams>? = null

        if (record != null) {
            val authRequest: SignRpc.SessionAuthenticate? = serializer.tryDeserialize<SignRpc.SessionAuthenticate>(record.body)
            if (authRequest != null) {
                entry = record.toRequest(authRequest.params)
            }
        }

        return entry
    }
}