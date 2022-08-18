@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.domain

import com.walletconnect.android_core.common.model.sync.PendingRequest
import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.json_rpc.domain.JsonRpcInteractorAbstract
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import com.walletconnect.foundation.common.model.Topic

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    networkState: ConnectivityState,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : JsonRpcInteractorAbstract(relay, serializer, chaChaPolyCodec, jsonRpcHistory, networkState) {

    override fun getPendingRequests(topic: Topic): List<PendingRequest> {
        TODO("Not yet implemented")
    }
}