@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.model.type.ClientParams
import com.walletconnect.android.common.json_rpc.JsonRpcSerializerAbstract
import com.walletconnect.sign.common.model.vo.clientsync.pairing.PairingRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.Empty

internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_SESSION_SETTLE -> tryDeserialize<SessionRpcVO.SessionSettle>(json)?.params
            JsonRpcMethod.WC_SESSION_REQUEST -> tryDeserialize<SessionRpcVO.SessionRequest>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<SessionRpcVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<SessionRpcVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_EVENT -> tryDeserialize<SessionRpcVO.SessionEvent>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<SessionRpcVO.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_EXTEND -> tryDeserialize<SessionRpcVO.SessionExtend>(json)?.params
            else -> null
        }

    override fun sdkSpecificSerialize(payload: com.walletconnect.android.common.SerializableJsonRpc): String =
        when (payload) {
            is SessionRpcVO.SessionPing -> trySerialize(payload)
            is SessionRpcVO.SessionEvent -> trySerialize(payload)
            is SessionRpcVO.SessionUpdate -> trySerialize(payload)
            is SessionRpcVO.SessionExtend -> trySerialize(payload)
            is SessionRpcVO.SessionRequest -> trySerialize(payload)
            is SessionRpcVO.SessionDelete -> trySerialize(payload)
            is SessionRpcVO.SessionSettle -> trySerialize(payload)
            else -> String.Empty
        }
}