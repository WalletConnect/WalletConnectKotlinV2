@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.data

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.ClientParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod

//
//internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {
//
//    override fun deserialize(method: String, json: String): ClientParams? = when (method) {
//        JsonRpcMethod.WC_AUTH_REQUEST -> tryDeserialize<AuthRpc.AuthRequest>(json)?.params
//        else -> null
//    }
//
//    override fun sdkSpecificSerialize(payload: SerializableJsonRpc): String = when (payload) {
//        is AuthRpc.AuthRequest -> trySerialize(payload)
//        else -> String.Empty
//    }
//
//}


internal class JsonRpcSerializerAddon(
    val serializerEntries: MutableSet<(SerializableJsonRpc) -> Boolean>,
    val deserializerEntries: MutableList<Pair<(String) -> Boolean, (String) -> ClientParams?>>,
) {

    init {
        addSerializeEntries()
        addDeserializeEntries()
    }

    private fun addSerializeEntries() {
        serializerEntries.add { payload: SerializableJsonRpc -> payload is AuthRpc.AuthRequest }
    }

    private fun addDeserializeEntries() {
        deserializerEntries.add(
            Pair({ method: String -> method == JsonRpcMethod.WC_AUTH_REQUEST }, { json: String -> tryDeserialize<AuthRpc.AuthRequest>(json)?.params })
        )
    }
}