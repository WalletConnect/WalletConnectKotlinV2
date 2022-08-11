@file:JvmSynthetic

package com.walletconnect.android_core.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.type.SerializableJsonRpc

abstract class JsonRpcSerializerAbstract(open val moshi: Moshi) {
    abstract fun deserialize(method: String, json: String): ClientParams?
    abstract fun serialize(payload: SerializableJsonRpc): String

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}