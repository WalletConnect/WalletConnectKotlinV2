package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.JsonRpcHistoryEntry
import com.walletconnect.auth.json_rpc.model.toEntry

internal class GetPendingJsonRpcHistoryEntryByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): JsonRpcHistoryEntry? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: JsonRpcHistoryEntry? = null

        if (record != null) {
            val authRequest: AuthRpc.AuthRequest? = serializer.tryDeserialize<AuthRpc.AuthRequest>(record.body)
            if (authRequest != null) {
                entry = record.toEntry(authRequest.params)
            }
        }

        return entry
    }
}