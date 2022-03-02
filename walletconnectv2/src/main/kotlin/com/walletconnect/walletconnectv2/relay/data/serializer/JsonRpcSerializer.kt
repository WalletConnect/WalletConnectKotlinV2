package com.walletconnect.walletconnectv2.relay.data.serializer

import com.squareup.moshi.Moshi
import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.type.SerializableJsonRpc
import com.walletconnect.walletconnectv2.core.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SharedKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.SettlementPairingVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.PostSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.PreSettlementSessionVO
import com.walletconnect.walletconnectv2.core.model.vo.payload.EncryptionPayloadVO
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.hexToUtf8

internal class JsonRpcSerializer(
    private val authenticatedEncryptionCodec: Codec,
    private val crypto: CryptoRepository,
    private val moshi: Moshi
) {

    internal fun encode(payload: String, topic: TopicVO): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            //TODO: symmetric ket encryption over topic A
            //val symmetricKey: SymmetricKey = crypto.generateSymmetricKey(topic)
            //publicKey is hash from symKey
            //symmetric decryption  authenticatedEncryptionCodec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
            payload.encode()
        } else {
            authenticatedEncryptionCodec.encrypt(payload, sharedKey as SharedKey, selfPublic as PublicKey)
        }
    }

    internal fun decode(message: String, topic: TopicVO): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            //TODO: symmetric ket decryption over topic A
            // val symmetricKey: SymmetricKey = crypto.generateSymmetricKey(topic)
            //symmetric decryption  authenticatedEncryptionCodec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
            message.hexToUtf8
        } else {
            authenticatedEncryptionCodec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
        }
    }

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<SettlementPairingVO.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PreSettlementSessionVO.Proposal>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<PostSettlementSessionVO.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<PostSettlementSessionVO.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_UPGRADE -> tryDeserialize<PostSettlementSessionVO.SessionUpgrade>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<PostSettlementSessionVO.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_NOTIFY -> tryDeserialize<PostSettlementSessionVO.SessionNotification>(json)?.params
            else -> null
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()

    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is SettlementPairingVO.PairingPayload -> trySerialize(payload)
            is SettlementPairingVO.PairingPing -> trySerialize(payload)
//            is PreSettlementSessionVO.Approve -> trySerialize(payload)
//            is PreSettlementSessionVO.Reject -> trySerialize(payload)
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