@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.domain

import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.json_rpc.model.toPendingRequest

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    fun getPendingRequests(topic: Topic): List<PendingRequest> =
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { record -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(record.body) != null }
            .map { record -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(record.body)!!.toPendingRequest(record) }
}