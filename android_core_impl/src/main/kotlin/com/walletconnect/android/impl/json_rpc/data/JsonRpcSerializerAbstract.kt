package com.walletconnect.android.impl.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.android.api.SerializableJsonRpc
import com.walletconnect.android.api.JsonRpc

abstract class JsonRpcSerializerAbstract(open val moshi: Moshi) {
    abstract fun deserialize(method: String, json: String): ClientParams?
    abstract fun sdkSpecificSerialize(payload: SerializableJsonRpc): String

    fun serialize(payload: SerializableJsonRpc): String = when (payload) {
        is JsonRpc.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
        is JsonRpc.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
        else -> sdkSpecificSerialize(payload)
    }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}