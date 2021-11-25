package org.walletconnect.walletconnectv2.crypto.managers

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.data.PrivateKey
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.storage.KeyChain
import org.walletconnect.walletconnectv2.storage.KeyStore
import org.walletconnect.walletconnectv2.util.bytesToHex
import org.walletconnect.walletconnectv2.util.hexToBytes
import java.security.MessageDigest
import org.walletconnect.walletconnectv2.crypto.data.Key as WCKey

class LazySodiumCryptoManager(private val keyChain: KeyStore = KeyChain()) : CryptoManager {

    private val lazySodium: LazySodiumAndroid = LazySodiumAndroid(SodiumAndroid())

    override fun generateKeyPair(): PublicKey {
        val lsKeyPair = lazySodium.cryptoSignKeypair()
        val curve25519KeyPair = lazySodium.convertKeyPairEd25519ToCurve25519(lsKeyPair)
        val (publicKey, privateKey) = curve25519KeyPair.let { keyPair ->
            PublicKey(keyPair.publicKey.asHexString.lowercase()) to PrivateKey(keyPair.secretKey.asHexString.lowercase())
        }
        setKeyPair(publicKey, privateKey)
        return publicKey
    }

    override fun generateTopicAndSharedKey(self: PublicKey, peer: PublicKey): Pair<SharedKey, Topic> {
        val (publicKey, privateKey) = getKeyPair(self)
        val sharedKeyHex = lazySodium.cryptoScalarMult(privateKey.toKey(), peer.toKey()).asHexString.lowercase()
        val sharedKey = SharedKey(sharedKeyHex)
        val topic = generateTopic(sharedKey.keyAsHex)
        setEncryptionKeys(sharedKey, publicKey, Topic(topic.topicValue.lowercase()))
        return Pair(sharedKey, topic)
    }

    override fun setEncryptionKeys(sharedKey: SharedKey, publicKey: PublicKey, topic: Topic) {
        keyChain.setKey(topic.topicValue, sharedKey, publicKey)
    }

    override fun removeKeys(tag: String) {
        val (_, publicKey) = keyChain.getKeys(tag)
        with(keyChain) {
            deleteKeys(publicKey.lowercase())
            deleteKeys(tag)
        }
    }

    override fun getKeyAgreement(topic: Topic): Pair<SharedKey, PublicKey> {
        val (sharedKey, peerPublic) = keyChain.getKeys(topic.topicValue)
        return Pair(SharedKey(sharedKey), PublicKey(peerPublic))
    }

    internal fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey) {
        keyChain.setKey(publicKey.keyAsHex, publicKey, privateKey)
    }

    internal fun getKeyPair(wcKey: WCKey): Pair<PublicKey, PrivateKey> {
        val (publicKeyHex, privateKeyHex) = keyChain.getKeys(wcKey.keyAsHex)
        return Pair(PublicKey(publicKeyHex), PrivateKey(privateKeyHex))
    }

    private fun generateTopic(sharedKey: String): Topic {
        val messageDigest: MessageDigest = MessageDigest.getInstance(SHA_256)
        val hashedBytes: ByteArray = messageDigest.digest(sharedKey.hexToBytes())
        return Topic(hashedBytes.bytesToHex())
    }

    internal fun getSharedKeyUsingPrivate(self: PrivateKey, peer: PublicKey): String {
        return lazySodium.cryptoScalarMult(self.toKey(), peer.toKey()).asHexString
    }

    private fun WCKey.toKey(): Key {
        return Key.fromHexString(keyAsHex)
    }

    companion object {
        private const val SHA_256: String = "SHA-256"
    }
}