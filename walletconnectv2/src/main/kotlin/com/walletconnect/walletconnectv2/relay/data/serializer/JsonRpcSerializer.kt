package com.walletconnect.walletconnectv2.relay.data.serializer

import com.squareup.moshi.Moshi
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.type.SerializableJsonRpc
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.PairingSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.util.Empty

internal class JsonRpcSerializer(
    private val authenticatedEncryptionCodec: Codec,
    private val crypto: CryptoRepository,
    private val moshi: Moshi,
) {

    internal fun encode(payload: String, topic: TopicVO): String {
        val (secretKey, selfPublic) = crypto.getKeyAgreement(topic)
        return authenticatedEncryptionCodec.encrypt(payload, secretKey, selfPublic)
    }

    internal fun decode(message: String, topic: TopicVO): String {
        val (secretKey, _) = crypto.getKeyAgreement(topic)
        return authenticatedEncryptionCodec.decrypt(toEncryptionPayload(message), secretKey)
    }

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PairingSettlementVO.SessionPropose>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PairingSettlementVO.PairingPing>(json)?.params

            JsonRpcMethod.WC_SESSION_SETTLE -> tryDeserialize<SessionSettlementVO.SessionSettle>(json)?.params
            JsonRpcMethod.WC_SESSION_REQUEST -> tryDeserialize<SessionSettlementVO.SessionRequest>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<SessionSettlementVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<SessionSettlementVO.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_UPGRADE -> tryDeserialize<SessionSettlementVO.SessionUpgrade>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<SessionSettlementVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_NOTIFY -> tryDeserialize<SessionSettlementVO.SessionNotify>(json)?.params
            else -> null
        }

    internal fun deserializeJsonRpcResultWithParams(params: ClientParams, jsonRpcResult: RelayDO.JsonRpcResponse.JsonRpcResult): Any =
        when (params) {
            is PairingParamsVO.SessionProposeParams ->
                tryDeserialize<SessionParamsVO.ApprovalParams>(jsonRpcResult.result.toString()) ?: jsonRpcResult.result
            else -> jsonRpcResult.result
        }

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is PairingSettlementVO.SessionPropose -> trySerialize(payload)
            is PairingSettlementVO.PairingPing -> trySerialize(payload)
            is PairingSettlementVO.PairingDelete -> trySerialize(payload)

            is SessionSettlementVO.SessionNotify -> trySerialize(payload)
            is SessionSettlementVO.SessionPing -> trySerialize(payload)
            is SessionSettlementVO.SessionUpdate -> trySerialize(payload)
            is SessionSettlementVO.SessionUpgrade -> trySerialize(payload)
            is SessionSettlementVO.SessionRequest -> trySerialize(payload)
            is SessionSettlementVO.SessionDelete -> trySerialize(payload)
            is SessionSettlementVO.SessionSettle -> trySerialize(payload)
            is SessionSettlementVO.SessionExtend -> trySerialize(payload)
            is SessionParamsVO.ApprovalParams -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)

    private fun toEncryptionPayload(message: String): EncryptionPayloadVO {
        val pubKeyStartIndex = EncryptionPayloadVO.ivLength
        val macStartIndex = pubKeyStartIndex + EncryptionPayloadVO.publicKeyLength
        val cipherTextStartIndex = macStartIndex + EncryptionPayloadVO.macLength

        val iv = message.substring(0, pubKeyStartIndex)
        val publicKey = message.substring(pubKeyStartIndex, macStartIndex)
        val mac = message.substring(macStartIndex, cipherTextStartIndex)
        val cipherText = message.substring(cipherTextStartIndex, message.length)

        return EncryptionPayloadVO(iv, publicKey, mac, cipherText)
    }
}