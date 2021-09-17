package org.walletconnect.walletconnectv2.cypto.providers

import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import org.walletconnect.walletconnectv2.cypto.CryptoManager
import org.walletconnect.walletconnectv2.cypto.data.PrivateKey
import org.walletconnect.walletconnectv2.cypto.data.PublicKey
import java.nio.charset.StandardCharsets

class LazySodiumCryptoManager: CryptoManager {
    private val lazySodium = LazySodiumJava(SodiumJava(LibraryLoader.Mode.PREFER_BUNDLED), StandardCharsets.UTF_8)

    fun generateKeyPair(): PublicKey {
        val lsKeyPair = lazySodium.cryptoSignKeypair()
        val curve25519KeyPair = lazySodium.convertKeyPairEd25519ToCurve25519(lsKeyPair)
        val (publicKey, privateKey) = curve25519KeyPair.let { keyPair ->
            PublicKey(keyPair.publicKey.asHexString) to PrivateKey(keyPair.secretKey.asHexString)
        }

        // TODO: Store KeyPair in KeyStore by converting both keys back into ByteArrays, adding them together,
        //  then convert the combined ByteArray into a hexed string to be stored.
        //  Example in packages/client/src/controllers/crypto.ts line 127 of monoRepo

        return publicKey
    }

    fun generateRandomBytes32(): String {
        return ""
    }

    fun deriveSharedKey(privateKeyA: String, publicKeyB: String): String {
        return ""
    }

    fun sha256(msg: String): String {
        return ""
    }

    fun encrypt(/*params: CryptoTypes.EncryptParams*/): String {
        return ""
    }

    fun decrypt(/*params: CryptoTypes.DecryptParams*/): String {
        return ""
    }
}