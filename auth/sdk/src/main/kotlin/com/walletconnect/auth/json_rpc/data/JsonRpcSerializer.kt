@file:JvmSynthetic

package com.walletconnect.auth.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.model.type.ClientParams
import com.walletconnect.android.common.json_rpc.JsonRpcSerializerAbstract
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.Empty

internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? = when (method) {
        JsonRpcMethod.WC_AUTH_REQUEST -> tryDeserialize<AuthRpc.AuthRequest>(json)?.params
        else -> null
    }

    override fun sdkSpecificSerialize(payload: com.walletconnect.android.common.SerializableJsonRpc): String = when (payload) {
        is AuthRpc.AuthRequest -> trySerialize(payload)
        else -> String.Empty
    }

}