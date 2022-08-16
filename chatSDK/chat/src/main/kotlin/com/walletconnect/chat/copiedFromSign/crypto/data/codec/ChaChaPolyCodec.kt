@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.crypto.data.codec

import com.walletconnect.android_core.common.MissingParticipantsException
import com.walletconnect.android_core.common.MissingReceiverPublicKeyException
import com.walletconnect.android_core.common.UnknownEnvelopeTypeException
import com.walletconnect.chat.copiedFromSign.core.model.type.enums.EnvelopeType
import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.SymmetricKey
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.sync.ParticipantsVO
import com.walletconnect.chat.copiedFromSign.crypto.Codec
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.copiedFromSign.util.bytesToHex
import com.walletconnect.chat.copiedFromSign.util.hexToBytes
import com.walletconnect.chat.copiedFromSign.util.randomBytes
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer

/* Note:
* The cha20Poly1305.init() throws InvalidArgumentException in the debugging mode code but it doesn't affects the final method execution
* EnvelopeType.ZERO -> tp + iv + sb
* EnvelopeType.ONE -> tp + pk + iv + sb
 */

internal class ChaChaPolyCodec(private val keyManagementRepository: KeyManagementRepository) : Codec {

    private val cha20Poly1305 = ChaCha20Poly1305()

    @Throws(
        UnknownEnvelopeTypeException::class,
        MissingParticipantsException::class
    )
    override fun encrypt(topic: TopicVO, jsonRpcPayload: String, envelopeType: EnvelopeType, participants: ParticipantsVO?): String {
        val input = jsonRpcPayload.toByteArray(Charsets.UTF_8)
        val nonceBytes = randomBytes(NONCE_SIZE)

        return when (envelopeType.id) {
            EnvelopeType.ZERO.id -> encryptEnvelopeType0(topic, nonceBytes, input, envelopeType)
            EnvelopeType.ONE.id -> encryptEnvelopeType1(participants, nonceBytes, input, envelopeType)
            else -> throw UnknownEnvelopeTypeException("Unknown envelope type: ${envelopeType.id}")
        }
    }

    @Throws(
        UnknownEnvelopeTypeException::class,
        MissingReceiverPublicKeyException::class
    )
    override fun decrypt(topic: TopicVO, encryptedPayload: String): String {
        val encryptedPayloadBytes = Base64.decode(encryptedPayload)

        return when (val envelopeType = encryptedPayloadBytes.envelopeType) {
            EnvelopeType.ZERO.id -> decryptType0(topic, encryptedPayloadBytes)
            EnvelopeType.ONE.id -> decryptType1(encryptedPayloadBytes, keyManagementRepository.getInvitePublicKey(topic))
            else -> throw UnknownEnvelopeTypeException("Unknown envelope type: $envelopeType")
        }
    }

    private fun decryptType0(topic: TopicVO, encryptedPayloadBytes: ByteArray): String {
        val envelopeType = ByteArray(ENVELOPE_TYPE_SIZE)
        val nonce = ByteArray(NONCE_SIZE)
        val encryptedMessageBytes = ByteArray(encryptedPayloadBytes.size - NONCE_SIZE - ENVELOPE_TYPE_SIZE)

        //tp + iv + sb
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(encryptedPayloadBytes)
        byteBuffer.get(envelopeType)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedMessageBytes)

        val symmetricKey = keyManagementRepository.getSymmetricKey(topic)
        val decryptedTextBytes = decryptPayload(symmetricKey, nonce, encryptedMessageBytes)

        return String(decryptedTextBytes, Charsets.UTF_8)
    }

    private fun decryptType1(encryptedPayloadBytes: ByteArray, receiverPublicKey: PublicKey?): String {
        if (receiverPublicKey == null) throw MissingReceiverPublicKeyException("Missing receiver public key")

        val envelopeType = ByteArray(ENVELOPE_TYPE_SIZE)
        val nonce = ByteArray(NONCE_SIZE)
        val publicKey = ByteArray(KEY_SIZE)
        val encryptedMessageBytes = ByteArray(encryptedPayloadBytes.size - NONCE_SIZE - KEY_SIZE - ENVELOPE_TYPE_SIZE)

        //tp + pk + iv + sb
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(encryptedPayloadBytes)
        byteBuffer.get(envelopeType)
        byteBuffer.get(publicKey)
        byteBuffer.get(nonce)
        byteBuffer.get(encryptedMessageBytes)

        val peer = PublicKey(publicKey.bytesToHex()) // PubKey Y
        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(receiverPublicKey, peer)
        keyManagementRepository.setSymmetricKey(TopicVO(keyManagementRepository.getHash(receiverPublicKey.keyAsHex)), symmetricKey)
        val decryptedTextBytes = decryptPayload(symmetricKey, nonce, encryptedMessageBytes)

        return String(decryptedTextBytes, Charsets.UTF_8)
    }

    private fun encryptEnvelopeType0(topic: TopicVO, nonceBytes: ByteArray, input: ByteArray, envelopeType: EnvelopeType): String {
        val symmetricKey = keyManagementRepository.getSymmetricKey(topic)
        val cipherBytes = encryptPayload(symmetricKey, nonceBytes, input)
        val payloadSize = cipherBytes.size + NONCE_SIZE + ENVELOPE_TYPE_SIZE

        //tp + iv + sb
        val encryptedPayloadBytes = ByteBuffer.allocate(payloadSize)
            .put(envelopeType.id).put(nonceBytes).put(cipherBytes)
            .array()

        return Base64.toBase64String(encryptedPayloadBytes)
    }

    private fun encryptEnvelopeType1(
        participants: ParticipantsVO?,
        nonceBytes: ByteArray,
        input: ByteArray,
        envelopeType: EnvelopeType,
    ): String {
        if (participants == null) throw MissingParticipantsException("Missing participants when encrypting envelope type 1")
        val self = participants.senderPublicKey
        val selfBytes = self.keyAsHex.hexToBytes()
        val peer = participants.receiverPublicKey
        val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(self, peer)
        val cipherBytes = encryptPayload(symmetricKey, nonceBytes, input)
        val payloadSize = cipherBytes.size + NONCE_SIZE + ENVELOPE_TYPE_SIZE + selfBytes.size
        //tp + pk + iv + sb
        val encryptedPayloadBytes = ByteBuffer.allocate(payloadSize)
            .put(envelopeType.id).put(selfBytes).put(nonceBytes).put(cipherBytes)
            .array()

        return Base64.toBase64String(encryptedPayloadBytes)
    }

    private fun encryptPayload(key: SymmetricKey, nonce: ByteArray, input: ByteArray): ByteArray {
        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonce)
        cha20Poly1305.init(true, params)
        val cipherBytes = ByteArray(cha20Poly1305.getOutputSize(input.size))
        val outputSize = cha20Poly1305.processBytes(input, 0, input.size, cipherBytes, 0)
        cha20Poly1305.doFinal(cipherBytes, outputSize)
        return cipherBytes
    }

    private fun decryptPayload(key: SymmetricKey, nonce: ByteArray, input: ByteArray): ByteArray {
        val params = ParametersWithIV(KeyParameter(key.keyAsHex.hexToBytes()), nonce)
        cha20Poly1305.init(false, params)
        val decryptedTextBytes = ByteArray(cha20Poly1305.getOutputSize(input.size))
        val outputSize = cha20Poly1305.processBytes(input, 0, input.size, decryptedTextBytes, 0)
        cha20Poly1305.doFinal(decryptedTextBytes, outputSize)
        return decryptedTextBytes
    }

    private companion object {
        const val NONCE_SIZE = 12
        const val KEY_SIZE = 32
        const val ENVELOPE_TYPE_SIZE = 1
        val ByteArray.envelopeType: Byte get() = this[0]
    }
}