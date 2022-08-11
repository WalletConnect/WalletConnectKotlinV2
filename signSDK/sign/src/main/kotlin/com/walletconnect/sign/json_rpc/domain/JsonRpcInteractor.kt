@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.android_core.json_rpc.data.NetworkState
import com.walletconnect.android_core.json_rpc.domain.JsonRpcInteractorAbstract
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.android_core.common.model.vo.sync.PendingRequestVO
import com.walletconnect.sign.crypto.Codec
import com.walletconnect.sign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequestVO

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: NetworkState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : JsonRpcInteractorAbstract(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    override fun getPendingRequests(topic: Topic): List<PendingRequestVO> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

}