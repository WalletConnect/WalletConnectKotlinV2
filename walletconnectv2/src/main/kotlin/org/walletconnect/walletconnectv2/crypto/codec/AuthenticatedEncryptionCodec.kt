package org.walletconnect.walletconnectv2.crypto.codec

import org.walletconnect.walletconnectv2.crypto.Codec
import org.walletconnect.walletconnectv2.crypto.data.EncryptionPayload
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.crypto.data.SharedKey
import org.walletconnect.walletconnectv2.util.bytesToHex
import org.walletconnect.walletconnectv2.util.hexToBytes
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AuthenticatedEncryptionCodec : Codec {

    override fun encrypt(message: String, sharedKey: SharedKey, publicKey: PublicKey): String {
        val (encryptionKey, authenticationKey) = getKeys(sharedKey.keyAsHex)

        val data = message.toByteArray(Charsets.UTF_8)
        val iv: ByteArray = randomBytes(16)

        val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encryptionKey, AES_ALGORITHM), IvParameterSpec(iv))
        val cipherText: ByteArray = cipher.doFinal(data)
        val computedMac: String = computeHmac(cipherText, iv, authenticationKey, publicKey.keyAsHex.hexToBytes())

        return iv.bytesToHex() + publicKey.keyAsHex + computedMac + cipherText.bytesToHex()
    }

    override fun decrypt(payload: EncryptionPayload, sharedKey: SharedKey): String {
        val (encryptionKey, authenticationKey) = getKeys(sharedKey.keyAsHex)
        val data = payload.cipherText.hexToBytes()
        val iv = payload.iv.hexToBytes()
        val computedHmac = computeHmac(data, iv, authenticationKey, payload.publicKey.hexToBytes())

        if (computedHmac != payload.mac.lowercase()) {
            throw Exception("Invalid Hmac")
        }

        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, AES_ALGORITHM), IvParameterSpec(iv))
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }

    fun getKeys(sharedKey: String): Pair<ByteArray, ByteArray> {
        val hexKey = sharedKey.hexToBytes()
        val messageDigest: MessageDigest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashedKey: ByteArray = messageDigest.digest(hexKey)

        val aesKey: ByteArray = hashedKey.take(32).toByteArray()
        val hmacKey: ByteArray = hashedKey.takeLast(32).toByteArray()
        return Pair(aesKey, hmacKey)
    }

    private fun computeHmac(
        data: ByteArray,
        iv: ByteArray,
        authKey: ByteArray,
        publicKey: ByteArray
    ): String {
        val mac = Mac.getInstance(MAC_ALGORITHM)
        val payload = iv + publicKey + data
        mac.init(SecretKeySpec(authKey, MAC_ALGORITHM))
        return mac.doFinal(payload).bytesToHex()
    }

    private fun randomBytes(size: Int): ByteArray {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    companion object {
        private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val MAC_ALGORITHM = "HmacSHA256"
        private const val HASH_ALGORITHM = "SHA-512"
        private const val AES_ALGORITHM = "AES"
    }
}