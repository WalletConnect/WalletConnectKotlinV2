@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android.impl.common.model.sync.PendingRequest
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android.api.RelayConnectionInterface
import com.walletconnect.android.impl.network.data.connection.ConnectivityState
import com.walletconnect.android.impl.storage.JsonRpcHistory
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
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    override fun getPendingRequests(topic: Topic): List<PendingRequest> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

}