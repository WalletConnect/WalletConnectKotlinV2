package org.walletconnect.walletconnectv2.jsonrpc

import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.crypto.managers.LazySodiumCryptoManager
import org.walletconnect.walletconnectv2.jsonrpc.utils.ClientJsonRpcSerializer
import org.walletconnect.walletconnectv2.util.hexToUtf8

class JsonRpcSerializer : JsonRpcSerializing {

    private val codec: AuthenticatedEncryptionCodec = AuthenticatedEncryptionCodec()
    private val crypto: CryptoManager = LazySodiumCryptoManager()

    override fun serialize(payload: ClientSyncJsonRpc, topic: Topic): String {
        val json = ClientJsonRpcSerializer.serialize(payload)
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)

        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            json.encode()
        } else {
            codec.encrypt(json, sharedKey as SharedKey, selfPublic as PublicKey)
        }
    }

    override fun decode(message: String, topic: Topic): String {
        val (sharedKey, selfPublic) = crypto.getKeyAgreement(topic)
        return if (sharedKey.keyAsHex.isEmpty() || selfPublic.keyAsHex.isEmpty()) {
            message.hexToUtf8
        } else {
            codec.decrypt(toEncryptionPayload(message), sharedKey as SharedKey)
        }
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