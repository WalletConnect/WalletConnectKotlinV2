package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.JsonRpcHistoryEntry
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.toEntry

internal class GetPendingJsonRpcHistoryEntriesUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(): List<JsonRpcHistoryEntry> =
        jsonRpcHistory.getListOfPendingRecords()
        .filter { record -> record.method == JsonRpcMethod.WC_AUTH_REQUEST }
        .filter { record -> serializer.tryDeserialize<AuthRpc.AuthRequest>(record.body) != null }
        .map { record -> record.toEntry(serializer.tryDeserialize<AuthRpc.AuthRequest>(record.body)!!.params) }
}