@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.model.PendingRequest
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

    fun getPendingRequests(): List<PendingRequest> =
        jsonRpcHistory.getAllRequests()
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_AUTH_REQUEST }
            .filter { entry -> serializer.deserialize(entry.method, entry.body) != null }
            .map { entry -> serializer.tryDeserialize<AuthRpcDTO.AuthRequest>(entry.body)!!.toPendingRequest(entry) }
    //todo: improve this method
}