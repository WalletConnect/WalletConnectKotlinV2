package com.walletconnect.walletconnectv2.crypto.data.repository

import com.walletconnect.walletconnectv2.core.model.vo.PrivateKey
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.SecretKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.util.bytesToHex
import com.walletconnect.walletconnectv2.util.hexToBytes
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.math.ec.rfc7748.X25519
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import com.walletconnect.walletconnectv2.core.model.vo.Key as WCKey

internal class BouncyCastleCryptoRepository(private val keyChain: KeyStore) : CryptoRepository {

    override fun generateSymmetricKey(topic: TopicVO): SecretKey {
        val symmetricKey = createSymmetricKey().bytesToHex()
        val publicKey = PublicKey(sha256(symmetricKey))

        keyChain.setKeys(topic.value, SecretKey(symmetricKey), publicKey)
        return SecretKey(symmetricKey)
    }

    override fun setSymmetricKey(topic: TopicVO, symmetricKey: SecretKey) {
        val publicKey = PublicKey(sha256(symmetricKey.keyAsHex))
        keyChain.setKeys(topic.value, symmetricKey, publicKey)
    }

    override fun generateKeyPair(): PublicKey {
        val publicKey = ByteArray(KEY_SIZE)
        val privateKey = ByteArray(KEY_SIZE)

        X25519.generatePrivateKey(SecureRandom(ByteArray(KEY_SIZE)), privateKey)
        X25519.generatePublicKey(privateKey, 0, publicKey, 0)
        setKeyPair(PublicKey(publicKey.bytesToHex().lowercase()), PrivateKey(privateKey.bytesToHex().lowercase()))
        return PublicKey(publicKey.bytesToHex().lowercase())
    }

    internal fun getSharedKey(selfPrivate: PrivateKey, peerPublic: PublicKey): String {
        val sharedKeyBytes = ByteArray(KEY_SIZE)
        X25519.scalarMult(selfPrivate.keyAsHex.hexToBytes(), 0, peerPublic.keyAsHex.hexToBytes(), 0, sharedKeyBytes, 0)
        return sharedKeyBytes.bytesToHex()
    }

    override fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SecretKey, TopicVO> {
        val (publicKey, privateKey) = getKeyPair(self)
        val sharedSecretBytes = ByteArray(KEY_SIZE)
        X25519.scalarMult(privateKey.keyAsHex.hexToBytes(), 0, peer.keyAsHex.hexToBytes(), 0, sharedSecretBytes, 0)

        val sharedSecret = sharedSecretBytes.bytesToHex()
        val sharedKeyBytes = deriveHKDFKey(sharedSecret)
        val secretKey = SecretKey(sharedKeyBytes.bytesToHex())
        val topic = TopicVO(sha256(secretKey.keyAsHex))
        setEncryptionKeys(secretKey, publicKey, TopicVO(topic.value.lowercase()))

        return Pair(secretKey, topic)
    }

    override fun setEncryptionKeys(sharedKey: SecretKey, publicKey: PublicKey, topic: TopicVO) {
        keyChain.setKeys(topic.value, sharedKey, publicKey)
    }

    override fun removeKeys(topic: String) {
        val (_, publicKey) = keyChain.getKeys(topic)
        with(keyChain) {
            deleteKeys(publicKey.lowercase())
            deleteKeys(topic)
        }
    }

    override fun getKeyAgreement(topic: TopicVO): Pair<SecretKey, PublicKey> {
        val (secretKey, peerPublic) = keyChain.getKeys(topic.value)
        return Pair(SecretKey(secretKey), PublicKey(peerPublic))
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
        private const val KEY_SIZE: Int = 32
        private const val SYM_KEY_SIZE: Int = 256
        private const val SHA_256: String = "SHA-256"
        private const val AES: String = "AES"
    }
}