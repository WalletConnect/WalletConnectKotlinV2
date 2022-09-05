@file:JvmSynthetic

package com.walletconnect.android_core.crypto.data.repository

import com.walletconnect.android_core.common.model.SymmetricKey
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.crypto.KeyStore
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import com.walletconnect.utils.Empty
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.math.ec.rfc7748.X25519
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import com.walletconnect.foundation.common.model.Key as WCKey

//todo: Refactor
internal class BouncyCastleKeyManagementRepository(private val keyChain: KeyStore) : KeyManagementRepository {
    override fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey {
        val symmetricKey = generateSymmetricKey()
        keyChain.setKey(topic.value, symmetricKey)
        return symmetricKey
    }

    override fun generateSymmetricKey(): SymmetricKey = SymmetricKey(createSymmetricKey().bytesToHex())

    override fun setSymmetricKey(topic: Topic, symmetricKey: SymmetricKey) {
        keyChain.setKey(topic.value, symmetricKey)
    }

    override fun getSymmetricKey(topic: Topic): SymmetricKey {
        val symmetricKey = keyChain.getKey(topic.value)

        return SymmetricKey(symmetricKey)
    }

    override fun generateKeyPair(): PublicKey {
        val publicKey = ByteArray(KEY_SIZE)
        val privateKey = ByteArray(KEY_SIZE)
        X25519.generatePrivateKey(SecureRandom(ByteArray(KEY_SIZE)), privateKey)
        X25519.generatePublicKey(privateKey, 0, publicKey, 0)
        setKeyPair(PublicKey(publicKey.bytesToHex().lowercase()), PrivateKey(privateKey.bytesToHex().lowercase()))

        return PublicKey(publicKey.bytesToHex().lowercase())
    }

    override fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic {
        val symmetricKey = generateSymmetricKeyFromKeyAgreement(self, peer)
        val topic = Topic(sha256(symmetricKey.keyAsHex))
        keyChain.setKey(topic.value.lowercase(), symmetricKey)
        setKeyAgreement(topic, self, peer)

        return topic
    }

    override fun getTopicFromKey(key: WCKey): Topic = Topic(sha256(key.keyAsHex))

    override fun setSelfParticipant(key: PublicKey, topic: Topic) {
        val tag = "$SELF_PARTICIPANT_CONTEXT${topic.value}"
        keyChain.setKey(tag, key)
    }

    override fun getSelfParticipant(topic: Topic): PublicKey? {
        val keyAsHex = keyChain.getKey("$SELF_PARTICIPANT_CONTEXT${topic.value}")
        return if (keyAsHex == String.Empty) null else PublicKey(keyAsHex)
    }

    override fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey {
        val (_, privateKey) = getKeyPair(self)
        val sharedSecretBytes = ByteArray(KEY_SIZE)
        X25519.scalarMult(privateKey.keyAsHex.hexToBytes(), 0, peer.keyAsHex.hexToBytes(), 0, sharedSecretBytes, 0)
        val sharedSecret = sharedSecretBytes.bytesToHex()
        val symmetricKeyBytes = deriveHKDFKey(sharedSecret)

        return SymmetricKey(symmetricKeyBytes.bytesToHex())
    }

    override fun removeKeys(tag: String) {
        val (publicKey, _) = keyChain.getKeys(tag)
        with(keyChain) {
            deleteKeys(publicKey.lowercase())
            deleteKeys(tag)
        }
    }

    override fun getKeyAgreement(topic: Topic): Pair<PublicKey, PublicKey> {
        val tag = "$KEY_AGREEMENT_CONTEXT${topic.value}"
        val (selfPublic, peerPublic) = keyChain.getKeys(tag)

        return Pair(PublicKey(selfPublic), PublicKey(peerPublic))
    }

    private fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey) {
        val tag = "$KEY_AGREEMENT_CONTEXT${topic.value}"
        keyChain.setKeys(tag, self, peer)
    }

    internal fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey) {
        keyChain.setKeys(publicKey.keyAsHex, publicKey, privateKey)
    }

    internal fun getKeyPair(wcKey: WCKey): Pair<PublicKey, PrivateKey> {
        val (publicKeyHex, privateKeyHex) = keyChain.getKeys(wcKey.keyAsHex)

        return Pair(PublicKey(publicKeyHex), PrivateKey(privateKeyHex))
    }

    private fun createSymmetricKey(): ByteArray {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(AES)
        keyGenerator.init(SYM_KEY_SIZE)

        return keyGenerator.generateKey().encoded
    }

    private fun sha256(key: String): String {
        val messageDigest: MessageDigest = MessageDigest.getInstance(SHA_256)
        val hashedBytes: ByteArray = messageDigest.digest(key.hexToBytes())

        return hashedBytes.bytesToHex()
    }

    private fun deriveHKDFKey(sharedSecret: String): ByteArray {
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        val inputKeyMaterial = sharedSecret.hexToBytes()
        val salt = ByteArray(0)
        val info = ByteArray(0)
        val hkdfParameters = HKDFParameters(inputKeyMaterial, salt, info)
        val derivedKey = ByteArray(KEY_SIZE)
        hkdf.init(hkdfParameters)
        hkdf.generateBytes(derivedKey, 0, KEY_SIZE)

        return derivedKey
    }

    private companion object {
        const val KEY_SIZE: Int = 32
        const val SYM_KEY_SIZE: Int = 256
        const val SHA_256: String = "SHA-256"
        const val AES: String = "AES"

        const val KEY_AGREEMENT_CONTEXT = "key_agreement/"
        const val SELF_PARTICIPANT_CONTEXT = "self_participant/"
    }
}