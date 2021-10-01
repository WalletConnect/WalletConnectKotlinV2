package org.walletconnect.walletconnectv2.crypto.managers

import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.HexMessageEncoder
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import com.goterl.lazysodium.utils.LibraryLoader
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.crypto.data.Key as WCKey
import org.walletconnect.walletconnectv2.crypto.CryptoManager
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.crypto.data.PrivateKey
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import java.nio.charset.StandardCharsets

class LazySodiumCryptoManager(private val keyChain: KeyChain): CryptoManager {
    private val lazySodium = LazySodiumJava(SodiumJava(LibraryLoader.Mode.PREFER_BUNDLED), StandardCharsets.UTF_8)

    fun hasKeys(tag: String): Boolean {
        return keyChain.getKey(tag).isNotBlank()
    }

    fun generateKeyPair(): PublicKey {
        val lsKeyPair = lazySodium.cryptoSignKeypair()
        val curve25519KeyPair = lazySodium.convertKeyPairEd25519ToCurve25519(lsKeyPair)
        val (publicKey, privateKey) = curve25519KeyPair.let { keyPair ->
            PublicKey(keyPair.publicKey.asHexString) to PrivateKey(keyPair.secretKey.asHexString)
        }

        setKeyPair(publicKey, privateKey)

        return publicKey
    }

    fun generateSharedKey(self: PublicKey, peer: PublicKey, overrideTopic: String?): Topic {
        val (publicKey, privateKey) = getKeyPair(self)
        val keyPair = KeyPair(privateKey.toKey(), peer.toKey())
        val sharedKey = lazySodium.cryptoBoxBeforeNm(keyPair)
        return setEncryptionKeys(sharedKey, publicKey, overrideTopic)
    }

    internal fun setEncryptionKeys(sharedKey: String, selfPublicKey: PublicKey, overrideTopic: String?): Topic {
        val topic = Topic(overrideTopic ?: lazySodium.cryptoHashSha256(selfPublicKey.key))
        val sharedKeyObject = object: WCKey {
            override val key: String = sharedKey
        }
        val keys = concatKeys(sharedKeyObject, selfPublicKey)

        keyChain.setKey(topic.topicValue, keys)

        return topic
    }

    internal fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey) {
        val keys = concatKeys(publicKey, privateKey)

        keyChain.setKey(publicKey.key, keys)
    }

    internal fun getKeyPair(wcKey: WCKey): Pair<PublicKey, PrivateKey> {
        val storageKey: String = keyChain.getKey(wcKey.key)

        return splitKeys(storageKey)
    }

    internal fun concatKeys(keyA: WCKey, keyB: WCKey): String {
        val encoder = HexMessageEncoder()
        return encoder.encode(encoder.decode(keyA.key) + encoder.decode(keyB.key))
    }

    internal fun splitKeys(concatKeys: String): Pair<PublicKey, PrivateKey> {
        val hexEncoder = HexMessageEncoder()
        val concatKeysByteArray = hexEncoder.decode(concatKeys)
        val privateKeyByteArray = concatKeysByteArray.sliceArray(0 until (concatKeysByteArray.size / 2))
        val publicKeyByteArray = concatKeysByteArray.sliceArray((concatKeysByteArray.size / 2) until concatKeysByteArray.size)

        return PublicKey(hexEncoder.encode(privateKeyByteArray)) to PrivateKey(hexEncoder.encode(publicKeyByteArray))
    }

    private fun WCKey.toKey(): Key {
        return Key.fromHexString(key)
    }
}