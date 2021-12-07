package org.walletconnect.walletconnectv2.jsonrpc

import org.walletconnect.walletconnectv2.ClientParams
import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.clientsync.pairing.after.PostSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.session.after.PostSettlementSession
import org.walletconnect.walletconnectv2.clientsync.session.before.PreSettlementSession
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.jsonrpc.utils.JsonRpcMethod
import org.walletconnect.walletconnectv2.moshi
import org.walletconnect.walletconnectv2.util.empty
import org.walletconnect.walletconnectv2.util.hexToUtf8

class JsonRpcSerializer {

    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager()

    fun serialize(payload: ClientSyncJsonRpc, topic: Topic): String {
        val json = serialize(payload)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            json.encode()
        } else {
            codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        }
    }

    fun decode(message: String, topic: Topic): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)
        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            message.hexToUtf8
        } else {
            codec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
        }
    }

    fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_PAIRING_APPROVE -> tryDeserialize<PreSettlementPairing.Approve>(json)?.params
            JsonRpcMethod.WC_PAIRING_REJECT -> tryDeserialize<PreSettlementPairing.Reject>(json)?.params
            JsonRpcMethod.WC_PAIRING_PAYLOAD -> tryDeserialize<PostSettlementPairing.PairingPayload>(json)?.params
            JsonRpcMethod.WC_PAIRING_UPDATE -> tryDeserialize<PostSettlementPairing.PairingUpdate>(json)?.params
            JsonRpcMethod.WC_PAIRING_PING -> tryDeserialize<PostSettlementPairing.PairingPing>(json)?.params
            JsonRpcMethod.WC_PAIRING_NOTIFICATION -> tryDeserialize<PostSettlementPairing.PairingPing>(json)?.params
            JsonRpcMethod.WC_SESSION_APPROVE -> tryDeserialize<PreSettlementSession.Approve>(json)?.params
            JsonRpcMethod.WC_SESSION_REJECT -> tryDeserialize<PreSettlementSession.Reject>(json)?.params
            JsonRpcMethod.WC_SESSION_PROPOSE -> tryDeserialize<PreSettlementSession.Proposal>(json)?.params
            JsonRpcMethod.WC_SESSION_PAYLOAD -> tryDeserialize<PostSettlementSession.SessionPayload>(json)?.params
            JsonRpcMethod.WC_SESSION_DELETE -> tryDeserialize<PostSettlementSession.SessionDelete>(json)?.params
            JsonRpcMethod.WC_SESSION_UPDATE -> tryDeserialize<PostSettlementSession.SessionUpdate>(json)?.params
            JsonRpcMethod.WC_SESSION_UPGRADE -> tryDeserialize<PostSettlementSession.SessionUpgrade>(json)?.params
            JsonRpcMethod.WC_SESSION_PING -> tryDeserialize<PostSettlementSession.SessionPing>(json)?.params
            JsonRpcMethod.WC_SESSION_NOTIFICATION -> tryDeserialize<PostSettlementSession.SessionNotification>(json)?.params
            else -> null
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()

    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)

    private fun serialize(payload: ClientSyncJsonRpc): String =
        when (payload) {
            is PreSettlementPairing.Approve -> trySerialize(payload)
            is PreSettlementPairing.Reject -> trySerialize(payload)
            is PostSettlementPairing.PairingPayload -> trySerialize(payload)
            is PostSettlementPairing.PairingNotification -> trySerialize(payload)
            is PostSettlementPairing.PairingPing -> trySerialize(payload)
            is PostSettlementPairing.PairingUpdate -> trySerialize(payload)
            is PreSettlementSession.Approve -> trySerialize(payload)
            is PreSettlementSession.Reject -> trySerialize(payload)
            is PreSettlementSession.Proposal -> trySerialize(payload)
            is PostSettlementSession.SessionNotification -> trySerialize(payload)
            is PostSettlementSession.SessionPing -> trySerialize(payload)
            is PostSettlementSession.SessionUpdate -> trySerialize(payload)
            is PostSettlementSession.SessionUpgrade -> trySerialize(payload)
            is PostSettlementSession.SessionPayload -> trySerialize(payload)
            is PostSettlementSession.SessionDelete -> trySerialize(payload)
            is JsonRpcResponse -> trySerialize(payload)
            else -> String.empty
        }

    private fun toEncryptionPayload(message: String): EncryptionPayload {
        val pubKeyStartIndex = EncryptionPayload.ivLength
        val macStartIndex = pubKeyStartIndex + EncryptionPayload.publicKeyLength
        val cipherTextStartIndex = macStartIndex + EncryptionPayload.macLength

        val iv = message.substring(0, pubKeyStartIndex)
        val publicKey = message.substring(pubKeyStartIndex, macStartIndex)
        val mac = message.substring(macStartIndex, cipherTextStartIndex)
        val cipherText = message.substring(cipherTextStartIndex, message.length)

        return EncryptionPayload(iv, publicKey, mac, cipherText)
    }

    private fun String.encode(): String = this.encodeToByteArray().joinToString(separator = "") { bytes -> String.format("%02X", bytes) }
}