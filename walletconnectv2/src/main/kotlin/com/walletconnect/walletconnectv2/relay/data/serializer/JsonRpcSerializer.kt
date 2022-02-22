package com.walletconnect.walletconnectv2.relay.data.serializer

import com.squareup.moshi.Moshi
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.type.SerializableJsonRpc
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SharedKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.after.PostSettlementPairingVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.before.PreSettlementPairingVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.PreSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.hexToUtf8

internal class JsonRpcSerializer(private val codec: Codec, private val crypto: CryptoRepository, private val moshi: Moshi) {

    internal fun encode(payload: String, topic: TopicVO): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            payload.encode()
        } else {
            codec.encrypt(payload, sharedKey as SharedKey, selfPublic as PublicKey)
        }
    }

    internal fun decode(message: String, topic: TopicVO): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            message.hexToUtf8
        } else {
            codec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
        }
    }

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_PAIRING_APPROVE -> tryDeserialize<PreSettlementPairingVO.Approve>(json)?.params
            JsonRpcMethod.WC_PAIRING_REJECT -> tryDeserialize<PreSettlementPairingVO.Reject>(json)?.params
            JsonRpcMethod.WC_PAIRING_PAYLOAD -> tryDeserialize<PostSettlementPairingVO.PairingPayload>(json)?.params
            JsonRpcMethod.WC_PAIRING_UPDATE -> tryDeserialize<PostSettlementPairingVO.PairingUpdate>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PostSettlementPairingVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_PAIRING_NOTIFICATION -> tryDeserialize<PostSettlementPairingVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_APPROVE -> tryDeserialize<PreSettlementSessionVO.Approve>(json)?.params
            JsonRpcMethod.WC_SESSION_REJECT -> tryDeserialize<PreSettlementSessionVO.Reject>(json)?.params
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PreSettlementSessionVO.Proposal>(json)?.params
            JsonRpcMethod.WC_SESSION_PAYLOAD -> tryDeserialize<PostSettlementSessionVO.SessionPayload>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<PostSettlementSessionVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<PostSettlementSessionVO.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_UPGRADE -> tryDeserialize<PostSettlementSessionVO.SessionUpgrade>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<PostSettlementSessionVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_NOTIFICATION -> tryDeserialize<PostSettlementSessionVO.SessionNotification>(json)?.params
            else -> null
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()

    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is PreSettlementPairingVO.Approve -> trySerialize(payload)
            is PreSettlementPairingVO.Reject -> trySerialize(payload)
            is PostSettlementPairingVO.PairingPayload -> trySerialize(payload)
            is PostSettlementPairingVO.PairingNotification -> trySerialize(payload)
            is PostSettlementPairingVO.PairingPing -> trySerialize(payload)
            is PostSettlementPairingVO.PairingUpdate -> trySerialize(payload)
            is PreSettlementSessionVO.Approve -> trySerialize(payload)
            is PreSettlementSessionVO.Reject -> trySerialize(payload)
            is PreSettlementSessionVO.Proposal -> trySerialize(payload)
            is PostSettlementSessionVO.SessionNotification -> trySerialize(payload)
            is PostSettlementSessionVO.SessionPing -> trySerialize(payload)
            is PostSettlementSessionVO.SessionUpdate -> trySerialize(payload)
            is PostSettlementSessionVO.SessionUpgrade -> trySerialize(payload)
            is PostSettlementSessionVO.SessionPayload -> trySerialize(payload)
            is PostSettlementSessionVO.SessionDelete -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

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

    private fun String.encode(): String = this.encodeToByteArray().joinToString(separator = "") { bytes -> String.format("%02X", bytes) }
}