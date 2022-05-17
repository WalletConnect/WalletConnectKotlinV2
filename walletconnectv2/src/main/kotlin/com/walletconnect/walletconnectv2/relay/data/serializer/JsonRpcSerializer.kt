package com.walletconnect.walletconnectv2.relay.data.serializer

import com.squareup.moshi.Moshi
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.type.SerializableJsonRpc
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.Logger

internal class JsonRpcSerializer(
    private val authenticatedEncryptionCodec: Codec,
    private val crypto: CryptoRepository,
    private val moshi: Moshi,
) {

    internal fun encode(payload: String, topic: TopicVO): String {
        val symmetricKey = crypto.getSymmetricKey(topic)
        return authenticatedEncryptionCodec.encrypt(payload, symmetricKey)
    }

    internal fun decode(message: String, topic: TopicVO): String {
        return try {
            val symmetricKey = crypto.getSymmetricKey(topic)
            authenticatedEncryptionCodec.decrypt(message, symmetricKey)
        } catch (e: Exception) {
            Logger.error("Decoding error: ${e.message}")
            String.Empty
        }
    }

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingSettlementVO.SessionPropose>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingSettlementVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_SETTLE -> tryDeserialize<SessionSettlementVO.SessionSettle>(json)?.params
            JsonRpcMethod.WC_SESSION_REQUEST -> tryDeserialize<SessionSettlementVO.SessionRequest>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<SessionSettlementVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<SessionSettlementVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_EVENT -> tryDeserialize<SessionSettlementVO.SessionEvent>(json)?.params
//            JsonRpcMethod.WC_SESSION_UPDATE_ACCOUNTS -> tryDeserialize<SessionSettlementVO.SessionUpdateAccounts>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE_NAMESPACES -> tryDeserialize<SessionSettlementVO.SessionUpdateNamespaces>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE_EXPIRY -> tryDeserialize<SessionSettlementVO.SessionUpdateExpiry>(json)?.params
            else -> null
        }

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is PairingSettlementVO.SessionPropose -> trySerialize(payload)
            is PairingSettlementVO.PairingPing -> trySerialize(payload)
            is PairingSettlementVO.PairingDelete -> trySerialize(payload)
            is SessionSettlementVO.SessionPing -> trySerialize(payload)
            is SessionSettlementVO.SessionEvent -> trySerialize(payload)
//            is SessionSettlementVO.SessionUpdateAccounts -> trySerialize(payload)
            is SessionSettlementVO.SessionUpdateNamespaces -> trySerialize(payload)
            is SessionSettlementVO.SessionUpdateExpiry -> trySerialize(payload)
            is SessionSettlementVO.SessionRequest -> trySerialize(payload)
            is SessionSettlementVO.SessionDelete -> trySerialize(payload)
            is SessionSettlementVO.SessionSettle -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}