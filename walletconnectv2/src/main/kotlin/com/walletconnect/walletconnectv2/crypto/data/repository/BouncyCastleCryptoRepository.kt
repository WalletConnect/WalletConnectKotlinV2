package com.walletconnect.walletconnectv2.crypto.data.repository

import com.walletconnect.walletconnectv2.core.model.vo.*
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.util.bytesToHex
import com.walletconnect.walletconnectv2.util.hexToBytes
import com.walletconnect.walletconnectv2.util.randomBytes
import org.bouncycastle.math.ec.rfc7748.X25519
import java.security.MessageDigest
import java.security.SecureRandom
import com.walletconnect.walletconnectv2.core.model.vo.Key as WCKey

internal class BouncyCastleCryptoRepository(private val keyChain: KeyStore) : CryptoRepository {

    override fun generateSymmetricKey(topic: TopicVO): SymmetricKey {
        //todo: is symKey valid?
        val symmetricKey = randomBytes(KEY_SIZE).bytesToHex()
        val publicKey = PublicKey(sha256(symmetricKey))

        keyChain.setKeys(topic.value, SymmetricKey(symmetricKey), publicKey)
        return SymmetricKey(symmetricKey)
    }

    override fun setSymmetricKey(topic: TopicVO, symmetricKey: SymmetricKey) {
        val publicKey = PublicKey(sha256(symmetricKey.keyAsHex))
        keyChain.setKeys(topic.value, symmetricKey, publicKey)
    }

    override fun getSymmetricKeys(topic: TopicVO): Pair<SymmetricKey, PublicKey> {
        val (symmetricKey, peerPublic) = keyChain.getKeys(topic.value)
        return Pair(SymmetricKey(symmetricKey), PublicKey(peerPublic))
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

    override fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, TopicVO> {
        val (publicKey, privateKey) = getKeyPair(self)
        val sharedKeyBytes = ByteArray(KEY_SIZE)
        X25519.scalarMult(privateKey.keyAsHex.hexToBytes(), 0, peer.keyAsHex.hexToBytes(), 0, sharedKeyBytes, 0)
        val sharedKey = SharedKey(sharedKeyBytes.bytesToHex())
        val topic = TopicVO(sha256(sharedKey.keyAsHex))
        setEncryptionKeys(sharedKey, publicKey, TopicVO(topic.value.lowercase()))
        return Pair(sharedKey, topic)
    }

    override fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: TopicVO) {
        keyChain.setKeys(topic.value, sharedKey, publicKey)
    }

    override fun removeKeys(topic: String) {
        val (_, publicKey) = keyChain.getKeys(topic)
        with(keyChain) {
            deleteKeys(publicKey.lowercase())
            deleteKeys(topic)
            //todo: add separate method for sym key deletion?
        }
    }

    override fun getKeyAgreement(topic: TopicVO): Pair<SharedKey, PublicKey> {
        val (sharedKey, peerPublic) = keyChain.getKeys(topic.value)
        return Pair(SharedKey(sharedKey), PublicKey(peerPublic))
    }

    internal fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey) {
        keyChain.setKeys(publicKey.keyAsHex, publicKey, privateKey)
    }

    internal fun getKeyPair(wcKey: WCKey): Pair<PublicKey, PrivateKey> {
        val (publicKeyHex, privateKeyHex) = keyChain.getKeys(wcKey.keyAsHex)
        return Pair(PublicKey(publicKeyHex), PrivateKey(privateKeyHex))
    }

    private fun sha256(key: String): String {
        val messageDigest: MessageDigest = MessageDigest.getInstance(SHA_256)
        val hashedBytes: ByteArray = messageDigest.digest(key.hexToBytes())
        return hashedBytes.bytesToHex()
    }

    private companion object {
        private const val KEY_SIZE: Int = 32
        private const val SHA_256: String = "SHA-256"
    }
}