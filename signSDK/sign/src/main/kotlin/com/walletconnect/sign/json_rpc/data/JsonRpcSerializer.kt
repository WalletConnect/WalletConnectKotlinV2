@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.type.SerializableJsonRpc
import com.walletconnect.android_core.json_rpc.data.JsonRpcSerializerAbstract
import com.walletconnect.android_core.json_rpc.model.JsonRpc
import com.walletconnect.sign.core.model.vo.clientsync.pairing.PairingRpcVO
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.Empty

internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingRpcVO.SessionPropose>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingRpcVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_SETTLE -> tryDeserialize<SessionRpcVO.SessionSettle>(json)?.params
            JsonRpcMethod.WC_SESSION_REQUEST -> tryDeserialize<SessionRpcVO.SessionRequest>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<SessionRpcVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<SessionRpcVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_EVENT -> tryDeserialize<SessionRpcVO.SessionEvent>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<SessionRpcVO.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_EXTEND -> tryDeserialize<SessionRpcVO.SessionExtend>(json)?.params
            else -> null
        }

    override fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is PairingRpcVO.SessionPropose -> trySerialize(payload)
            is PairingRpcVO.PairingPing -> trySerialize(payload)
            is PairingRpcVO.PairingDelete -> trySerialize(payload)
            is SessionRpcVO.SessionPing -> trySerialize(payload)
            is SessionRpcVO.SessionEvent -> trySerialize(payload)
            is SessionRpcVO.SessionUpdate -> trySerialize(payload)
            is SessionRpcVO.SessionExtend -> trySerialize(payload)
            is SessionRpcVO.SessionRequest -> trySerialize(payload)
            is SessionRpcVO.SessionDelete -> trySerialize(payload)
            is SessionRpcVO.SessionSettle -> trySerialize(payload)
            is JsonRpc.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is JsonRpc.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }
}