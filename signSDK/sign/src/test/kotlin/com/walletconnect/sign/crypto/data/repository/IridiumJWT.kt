package com.walletconnect.sign.crypto.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.crypto.data.repository.model.IridiumJWTHeader
import com.walletconnect.sign.crypto.data.repository.model.IridiumJWTPayload
import com.walletconnect.sign.util.hexToBytes
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.ByteBuffer
import java.util.*
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class IridiumJWTTest {
    // Client will sign the Server assigned socketId as a nonce
    private val nonce = "c479fe5dc464e771e78b193d239a65b58d278cad1c34bfb0b5716e5bb514928e"

    // Generate key pair from seed
    private val keyHexBase64ByteArray = "58e0254c211b858ef7896b00e3f36beeb13d568d47c6031c4218b87718061295884ab67f787b69e534bfdba8d5beb4e719700e90ac06317ed177d49e5a33be5a".hexToBytes()
    private val keyPairByteArray = ByteBuffer.wrap(keyHexBase64ByteArray).let { buffer ->
        ByteArray(32).apply { buffer.get(this) } to ByteArray(buffer.remaining()).apply { buffer.get(this) }
    }

    // Expected JWT for given nonce
    private val expected =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6a2V5Ono2TWtvZEhad25lVlJTaHRhTGY4SktZa3hwREdwMXZHWm5wR21kQnBYOE0yZXh4SCIsInN1YiI6ImM0NzlmZTVkYzQ2NGU3NzFlNzhiMTkzZDIzOWE2NWI1OGQyNzhjYWQxYzM0YmZiMGI1NzE2ZTViYjUxNDkyOGUifQ.0JkxOM-FV21U7Hk-xycargj_qNRaYV2H5HYtE4GzAeVQYiKWj7YySY5AdSqtCgGzX4Gt98XWXn2kSr9rE1qvCA"

    @Test
    fun signJWTTest() {
        val jwt = signJWT(nonce)
        assertEquals(expected, jwt)
    }

    private fun signJWT(subject: String): String {
        val (secretKeyByteArray, publicKeyByteArray) = keyPairByteArray
        val publicKeyParameters = Ed25519PublicKeyParameters(publicKeyByteArray)
        val secretKeyParameters = Ed25519PrivateKeyParameters(secretKeyByteArray, 0)

        val issuer = encodeIss(publicKeyParameters)
        val payload = IridiumJWTPayload(issuer, subject)
        val data = encodeData(JWT_IRIDIUM_HEADER, payload).encodeToByteArray()

        val signature = Ed25519Signer().run {
            init(true, secretKeyParameters)
            update(data, 0, data.size)
            generateSignature()
        }

        return encodeJWT(JWT_IRIDIUM_HEADER, payload, signature)
    }

    private fun encodeIss(publicKey: Ed25519PublicKeyParameters): String {
        val header: ByteArray = Multibase.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey.encoded)

        return listOf(DID_PREFIX, DID_METHOD, multicodec).joinToString(DID_DELIMITER)
    }

    private fun encodeData(header: IridiumJWTHeader, payload: IridiumJWTPayload): String {
        return listOf(encodeJSON(header), encodeJSON(payload)).joinToString(JWT_DELIMITER)
    }

    private fun encodeJWT(header: IridiumJWTHeader, payload: IridiumJWTPayload, signature: ByteArray): String {
        return listOf(encodeJSON(header), encodeJSON(payload), encodeSig(signature)).joinToString(JWT_DELIMITER)
    }

    private fun <T> encodeJSON(jsonObj: T): String {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
        val jsonByteArray = jsonString.toByteArray(Charsets.UTF_8)

        return Multibase.encode(Multibase.Base.Base64, jsonByteArray).substring(1)
    }

    private fun encodeSig(signature: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature).also { println(it + " 2") }
    }

    private companion object {
        val JWT_IRIDIUM_HEADER = IridiumJWTHeader(algorithm = "EdDSA", type = "JWT")
        const val JWT_DELIMITER = "."
        const val DID_DELIMITER = ":"
        const val DID_PREFIX = "did"
        const val DID_METHOD = "key"
        const val MULTICODEC_ED25519_HEADER = "zK36"
    }
}