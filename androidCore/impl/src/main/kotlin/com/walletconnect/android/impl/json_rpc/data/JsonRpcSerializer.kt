package com.walletconnect.android.impl.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.ClientParams
import com.walletconnect.android.common.model.JsonRpcClientSync
import kotlin.reflect.KClass

//todo: Verify if consequent initialisations of child class append moshi adapters and store entries.
class JsonRpcSerializer(
    val moshi: Moshi,
    val serializerEntries: MutableSet<(SerializableJsonRpc) -> Boolean>,
    val deserializerEntries: MutableMap<String, KClass<*>>,
) {

    fun deserialize(method: String, json: String): ClientParams? {
        val deserializedObject = tryDeserialize<JsonRpcClientSync<*>>(json) ?: return null
        val type = deserializerEntries[method]

        return if (deserializedObject::class == type) {
            deserializedObject.params
        } else {
            null
        }
    }

    fun serialize(payload: SerializableJsonRpc): String? = when {
        payload is JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
        payload is JsonRpcResponse.JsonRpcError -> trySerialize(payload)
        serializerEntries.any { rpcType: (SerializableJsonRpc) -> Boolean -> rpcType(payload) } -> trySerialize(payload)
        else -> null
    }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}
