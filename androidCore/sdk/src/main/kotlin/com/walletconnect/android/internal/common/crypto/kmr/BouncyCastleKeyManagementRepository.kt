@file:JvmSynthetic

package com.walletconnect.android.internal.common.crypto.kmr

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.common.model.Key
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.math.ec.rfc7748.X25519
import org.bouncycastle.math.ec.rfc8032.Ed25519
import java.security.SecureRandom
import javax.crypto.KeyGenerator

internal class BouncyCastleKeyManagementRepository(private val keyChain: KeyStore) : KeyManagementRepository {
    override fun setKey(key: Key, tag: String) {
        keyChain.setKey(tag, key)
    }

    @Throws(MissingKeyException::class)
    override fun getPublicKey(tag: String): PublicKey {
        val key = keyChain.getKey(tag) ?: throw MissingKeyException("No PublicKey for tag: $tag")
        return PublicKey(key)
    }

    @Throws(MissingKeyException::class)
    override fun getSymmetricKey(tag: String): SymmetricKey {
        val key = keyChain.getKey(tag) ?: throw MissingKeyException("No SymmetricKey for tag: $tag")
        return SymmetricKey(key)
    }

    @Throws(MissingKeyException::class)
    override fun getSelfPublicFromKeyAgreement(topic: Topic): PublicKey {
        val tag = "$KEY_AGREEMENT_CONTEXT${topic.value}"
        val (selfPublic, _) = keyChain.getKeys(tag) ?: throw MissingKeyException("No key pair for tag: $tag")

        return PublicKey(selfPublic)
    }

    override fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey) {
        val tag = "$KEY_AGREEMENT_CONTEXT${topic.value}"
        keyChain.setKeys(tag, self, peer)
    }

    override fun generateAndStoreEd25519KeyPair(): PublicKey {
        val publicKey = ByteArray(KEY_SIZE)
        val privateKey = ByteArray(KEY_SIZE)
        Ed25519.generatePrivateKey(SecureRandom(ByteArray(KEY_SIZE)), privateKey)
        Ed25519.generatePublicKey(privateKey, 0, publicKey, 0)

        setKeyPair(PublicKey(publicKey.bytesToHex().lowercase()), PrivateKey(privateKey.bytesToHex().lowercase()))
        return PublicKey(publicKey.bytesToHex().lowercase())
    }

    override fun generateAndStoreX25519KeyPair(): PublicKey {
        val publicKey = ByteArray(KEY_SIZE)
        val privateKey = ByteArray(KEY_SIZE)
        X25519.generatePrivateKey(SecureRandom(ByteArray(KEY_SIZE)), privateKey)
        X25519.generatePublicKey(privateKey, 0, publicKey, 0)

        setKeyPair(PublicKey(publicKey.bytesToHex().lowercase()), PrivateKey(privateKey.bytesToHex().lowercase()))
        return PublicKey(publicKey.bytesToHex().lowercase())
    }

    override fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey {
        val symmetricKey = SymmetricKey(createSymmetricKey().bytesToHex())
        keyChain.setKey(topic.value, symmetricKey)
        return symmetricKey
    }

    override fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey {
        val (_, privateKey) = getKeyPair(self)
        val sharedSecretBytes = ByteArray(KEY_SIZE)
        X25519.scalarMult(privateKey.keyAsHex.hexToBytes(), 0, peer.keyAsHex.hexToBytes(), 0, sharedSecretBytes, 0)
        val sharedSecret = sharedSecretBytes.bytesToHex()
        val symmetricKeyBytes = deriveHKDFKey(sharedSecret)

        return SymmetricKey(symmetricKeyBytes.bytesToHex())
    }

    override fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic {
        val symmetricKey = generateSymmetricKeyFromKeyAgreement(self, peer)
        val topic = Topic(sha256(symmetricKey.keyAsBytes))
        keyChain.setKey(topic.value.lowercase(), symmetricKey)
        setKeyAgreement(topic, self, peer)
        return topic
    }

    override fun getTopicFromKey(key: Key): Topic = Topic(sha256(key.keyAsBytes))

    @Throws(MissingKeyException::class)
    override fun removeKeys(tag: String) {
        val (publicKey, _) = keyChain.getKeys(tag) ?: throw MissingKeyException("No key pair for tag: $tag")
        with(keyChain) {
            deleteKeys(publicKey.lowercase())
            deleteKeys(tag)
        }
    }

    internal fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey) {
        keyChain.setKeys(publicKey.keyAsHex, publicKey, privateKey)
    }

    @Throws(MissingKeyException::class)
    override fun getKeyPair(key: PublicKey): Pair<PublicKey, PrivateKey> {
        val (publicKeyHex, privateKeyHex) = keyChain.getKeys(key.keyAsHex) ?: throw MissingKeyException("No key pair for tag: ${key.keyAsHex}")

        return Pair(PublicKey(publicKeyHex), PrivateKey(privateKeyHex))
    }

    private fun createSymmetricKey(): ByteArray {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(AES)
        keyGenerator.init(SYM_KEY_SIZE)

        return keyGenerator.generateKey().encoded
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
        const val AES: String = "AES"

        const val KEY_AGREEMENT_CONTEXT = "key_agreement/"
    }
}