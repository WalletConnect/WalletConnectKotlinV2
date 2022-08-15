@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android_core.common.model.sync.PendingRequest
import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.JsonRpcInteractorAbstract
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequestVO

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : JsonRpcInteractorAbstract(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    override fun getPendingRequests(topic: Topic): List<PendingRequest> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

}