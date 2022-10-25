package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.android.internal.common.JsonRpcResponse

internal class GetResponseByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): JsonRpcResponse? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getRecordById(id)
        var jsonRpcResponse: JsonRpcResponse? = null

        if (record != null) {
            record.response?.let { responseJson ->
                serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(responseJson)?.let { jsonRpcResult ->
                    jsonRpcResponse = jsonRpcResult
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(responseJson)?.let { jsonRpcError ->
                    jsonRpcResponse = jsonRpcError
                }
            }
        }

        return jsonRpcResponse
    }
}