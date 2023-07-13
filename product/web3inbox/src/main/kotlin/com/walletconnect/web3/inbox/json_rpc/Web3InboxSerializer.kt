package com.walletconnect.web3.inbox.json_rpc

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.SerializableJsonRpc
import com.walletconnect.android.internal.common.wcKoinApp

internal object Web3InboxSerializer {
    private val serializer: JsonRpcSerializer get() = wcKoinApp.koin.get()

    fun deserializeRpc(payload: String): Web3InboxRPC? = serializer.tryDeserialize<ClientJsonRpc>(payload)?.let { clientJsonRpc ->
        val type = serializer.deserializerEntries[clientJsonRpc.method] ?: return null
        val deserializedObject = serializer.tryDeserialize(payload, type) ?: return null

        return if (deserializedObject::class == type && deserializedObject is Web3InboxRPC) {
            deserializedObject
        } else {
            null
        }
    }

    fun serializeRpc(rpc: SerializableJsonRpc) = serializer.serialize(rpc)
}