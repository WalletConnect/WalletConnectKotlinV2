@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.relay.data.serializer

import com.squareup.moshi.Moshi
import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.copiedFromSign.core.model.type.SerializableJsonRpc
import com.walletconnect.chat.copiedFromSign.relay.model.RelayerDO
import com.walletconnect.chat.copiedFromSign.util.Empty

internal class JsonRpcSerializer(private val moshi: Moshi) {

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
//            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingSettlementVO.SessionPropose>(json)?.params
//            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingSettlementVO.PairingPing>(json)?.params
//            JsonRpcMethod.WC_SESSION_SETTLE -> tryDeserialize<SessionSettlementVO.SessionSettle>(json)?.params
//            JsonRpcMethod.WC_SESSION_REQUEST -> tryDeserialize<SessionSettlementVO.SessionRequest>(json)?.params
//            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<SessionSettlementVO.SessionDelete>(json)?.params
//            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<SessionSettlementVO.SessionPing>(json)?.params
//            JsonRpcMethod.WC_SESSION_EVENT -> tryDeserialize<SessionSettlementVO.SessionEvent>(json)?.params
//            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<SessionSettlementVO.SessionUpdate>(json)?.params
//            JsonRpcMethod.WC_SESSION_EXTEND -> tryDeserialize<SessionSettlementVO.SessionExtend>(json)?.params
            else -> null
        }

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
//            is PairingSettlementVO.SessionPropose -> trySerialize(payload)
//            is PairingSettlementVO.PairingPing -> trySerialize(payload)
//            is PairingSettlementVO.PairingDelete -> trySerialize(payload)
//            is SessionSettlementVO.SessionPing -> trySerialize(payload)
//            is SessionSettlementVO.SessionEvent -> trySerialize(payload)
//            is SessionSettlementVO.SessionUpdate -> trySerialize(payload)
//            is SessionSettlementVO.SessionExtend -> trySerialize(payload)
//            is SessionSettlementVO.SessionRequest -> trySerialize(payload)
//            is SessionSettlementVO.SessionDelete -> trySerialize(payload)
//            is SessionSettlementVO.SessionSettle -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}