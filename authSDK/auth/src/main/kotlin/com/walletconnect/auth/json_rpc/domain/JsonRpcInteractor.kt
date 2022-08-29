@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android_core.json_rpc.model.JsonRpcResponse
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.model.PendingRequestVO
import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.auth.json_rpc.model.toPendingRequest

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    fun getPendingRequests(): List<PendingRequestVO> =
        jsonRpcHistory.getPendingRequests()
            .filter { entry -> entry.method == JsonRpcMethod.WC_AUTH_REQUEST }
            .filter { rpcHistory -> serializer.tryDeserialize<AuthRpcDTO.AuthRequest>(rpcHistory.body) != null }
            .map { rpcHistory -> serializer.tryDeserialize<AuthRpcDTO.AuthRequest>(rpcHistory.body)!!.toPendingRequest(rpcHistory) }

    fun getPendingRequestById(id: Long): PendingRequestVO? {
        val jsonRpcHistory = jsonRpcHistory.getPendingRequestById(id)
        var pendingRequest: PendingRequestVO? = null

        if (jsonRpcHistory != null) {
           val authRequest: AuthRpcDTO.AuthRequest? = serializer.tryDeserialize<AuthRpcDTO.AuthRequest>(jsonRpcHistory.body)
            if (authRequest != null) {
                pendingRequest = authRequest.toPendingRequest(jsonRpcHistory)
            }
        }

        return pendingRequest
    }

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