package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.impl.storage.JsonRpcHistory

internal class GetResponseByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): com.walletconnect.android.common.JsonRpcResponse? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getRecordById(id)
        var jsonRpcResponse: com.walletconnect.android.common.JsonRpcResponse? = null

        if (record != null) {
            record.response?.let { responseJson ->
                serializer.tryDeserialize<com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult>(responseJson)?.let { jsonRpcResult ->
                    jsonRpcResponse = jsonRpcResult
                } ?: serializer.tryDeserialize<com.walletconnect.android.common.JsonRpcResponse.JsonRpcError>(responseJson)?.let { jsonRpcError ->
                    jsonRpcResponse = jsonRpcError
                }
            }
        }

        return jsonRpcResponse
    }
}