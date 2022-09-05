package com.walletconnect.android_core.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.type.SerializableJsonRpc
import com.walletconnect.android_core.json_rpc.model.JsonRpcResponse

abstract class JsonRpcSerializerAbstract(open val moshi: Moshi) {
    abstract fun deserialize(method: String, json: String): ClientParams?
    abstract fun sdkSpecificSerialize(payload: SerializableJsonRpc): String

    fun serialize(payload: SerializableJsonRpc): String = when (payload) {
        is JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
        is JsonRpcResponse.JsonRpcError -> trySerialize(payload)
        else -> sdkSpecificSerialize(payload)
    }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}