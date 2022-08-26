@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android_core.json_rpc.model.JsonRpcResponse
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.toEntry

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    fun getPendingRequests(): List<PendingRequest> =
        jsonRpcHistory.getPendingRequests()
            .filter { rpcHistory -> rpcHistory.method == JsonRpcMethod.WC_AUTH_REQUEST }
            .filter { rpcHistory -> serializer.deserialize(rpcHistory.method, rpcHistory.body) != null }
            .map { rpcHistory -> serializer.tryDeserialize<AuthRpcDTO.AuthRequest>(rpcHistory.body)!!.toEntry(rpcHistory) }

    fun getResponseById(id: Long): JsonRpcResponse? {
        val jsonRpcHistory = jsonRpcHistory.getRequestById(id)
        var jsonRpcResponse: JsonRpcResponse? = null

        if (jsonRpcHistory != null) {
            jsonRpcHistory.response?.let { responseJson ->
                serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(responseJson)?.let { rpcResult ->
                    jsonRpcResponse = rpcResult
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(responseJson)?.let { rpcError ->
                    jsonRpcResponse = rpcError
                }
            }
        }

        return jsonRpcResponse
    }
}