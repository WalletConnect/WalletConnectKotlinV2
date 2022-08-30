@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android.impl.common.model.sync.PendingRequest
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android.api.RelayConnectionInterface
import com.walletconnect.android.impl.network.data.connection.ConnectivityState
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import com.walletconnect.foundation.common.model.Topic

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    override fun getPendingRequests(topic: Topic): List<PendingRequest> {
        TODO("Not yet implemented")
    }
}