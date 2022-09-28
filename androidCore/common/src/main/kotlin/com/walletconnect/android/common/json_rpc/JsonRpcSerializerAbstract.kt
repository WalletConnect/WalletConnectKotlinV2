package com.walletconnect.android.common.json_rpc

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.model.type.ClientParams
import com.walletconnect.android.common.SerializableJsonRpc

abstract class JsonRpcSerializerAbstract(open val moshi: Moshi) {
    abstract fun sdkSpecificDeserialize(method: String, json: String): ClientParams?
    abstract fun sdkSpecificSerialize(payload: SerializableJsonRpc): String

    fun serialize(payload: SerializableJsonRpc): String = when (payload) {
        is JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
        is JsonRpcResponse.JsonRpcError -> trySerialize(payload)
        is PairingRpcVO.SessionPropose -> trySerialize(payload)
        is PairingRpcVO.PairingPing -> trySerialize(payload)
        is PairingRpcVO.PairingDelete -> trySerialize(payload)

        else -> sdkSpecificSerialize(payload)
    }

    fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingRpcVO.SessionPropose>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingRpcVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_PAIRING_DELETE -> tryDeserialize<PairingRpcVO.PairingDelete>(json)?.params
            else -> sdkSpecificDeserialize(method, json)
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}