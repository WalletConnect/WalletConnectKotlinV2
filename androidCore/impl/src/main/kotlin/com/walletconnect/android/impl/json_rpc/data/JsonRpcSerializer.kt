package com.walletconnect.android.impl.json_rpc.data

//abstract class JsonRpcSerializerAbstract(open val moshi: Moshi) {
//    abstract fun deserialize(method: String, json: String): ClientParams?
//    abstract fun sdkSpecificSerialize(payload: SerializableJsonRpc): String
//
//    fun serialize(payload: SerializableJsonRpc): String = when (payload) {
//        is JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
//        is JsonRpcResponse.JsonRpcError -> trySerialize(payload)
//        else -> sdkSpecificSerialize(payload)
//    }
//
//    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
//    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
//}

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.ClientParams
import com.walletconnect.utils.Empty

//todo: Verify if consequent initialisations of child class append moshi adapters and store entries.
class JsonRpcSerializer(
    val moshi: Moshi,
    val serializerEntries: MutableSet<(SerializableJsonRpc) -> Boolean>,
    val deserializerEntries: MutableMap<String, Class<ClientParams>>,
) {

    fun deserialize(method: String, json: String): ClientParams? {
        val type = deserializerEntries[method]!!
        return tryDeserialize<>(json)?.params
    }
//            ?.second?.let { it(json) }
//    JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingRpcVO.SessionPropose>(json)?.params

    fun serialize(payload: SerializableJsonRpc): String = when {
        payload is JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
        payload is JsonRpcResponse.JsonRpcError -> trySerialize(payload)
        serializerEntries.any { rpcType: (SerializableJsonRpc) -> Boolean -> rpcType(payload) } -> trySerialize(payload)
        else -> String.Empty
    }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}
