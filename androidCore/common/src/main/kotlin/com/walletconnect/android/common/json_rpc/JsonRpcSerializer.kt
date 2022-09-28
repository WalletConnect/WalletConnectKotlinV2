package com.walletconnect.android.common.json_rpc

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.model.type.ClientParams

internal class PairingJsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingRpcVO.PairingPing>(json)?.params
            else -> null
        }

    override fun sdkSpecificSerialize(payload: com.walletconnect.android.common.SerializableJsonRpc): String =
        when (payload) {
            is PairingRpcVO.PairingPing -> trySerialize(payload)
            is PairingRpcVO.PairingDelete -> trySerialize(payload)
            else -> String.Empty
        }
}