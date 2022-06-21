package com.walletconnect.sign.crypto.data.repository

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.util.hexToBytes
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.KeyGenerationParameters
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class IridiumJWTTest {

    @JsonClass(generateAdapter = true)
    data class IridiumJWTHeader(val algorithm: String, val type: String)

    @JsonClass(generateAdapter = true)
    data class IridiumJWTPayload(val issuer: String, val subject: String)

    data class IridiumJWTData(val header: IridiumJWTHeader, val payload: IridiumJWTPayload)

    data class IridiumJWTSigned(val signature: ByteArray, val header: IridiumJWTHeader, val payload: IridiumJWTPayload) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IridiumJWTSigned

            if (!signature.contentEquals(other.signature)) return false
            if (header != other.header) return false
            if (payload != other.payload) return false

            return true
        }

        override fun hashCode(): Int {
            var result = signature.contentHashCode()
            result = 31 * result + header.hashCode()
            result = 31 * result + payload.hashCode()
            return result
        }
    }

    // Client will sign the Server assigned socketId as a nonce
    private val nonce = "c479fe5dc464e771e78b193d239a65b58d278cad1c34bfb0b5716e5bb514928e";
    // Fixed seed to generate the same key pair
    private val seed = "58e0254c211b858ef7896b00e3f36beeb13d568d47c6031c4218b87718061295".hexToBytes()
    // Generate key pair from seed
    private val keyPair: AsymmetricCipherKeyPair
        get() = Ed25519KeyPairGenerator().run {
            this.init(KeyGenerationParameters(SecureRandom(seed), 255))
            this.generateKeyPair()
        }
    // Expected JWT for given nonce
    private val expected = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6a2V5Ono2TWtvZEhad25lVlJTaHRhTGY4SktZa3hwREdwMXZHWm5wR21kQnBYOE0yZXh4SCIsInN1YiI6ImM0NzlmZTVkYzQ2NGU3NzFlNzhiMTkzZDIzOWE2NWI1OGQyNzhjYWQxYzM0YmZiMGI1NzE2ZTViYjUxNDkyOGUifQ.0JkxOM-FV21U7Hk-xycargj_qNRaYV2H5HYtE4GzAeVQYiKWj7YySY5AdSqtCgGzX4Gt98XWXn2kSr9rE1qvCA"

    private fun signJWT(subject: String, keyPair: AsymmetricCipherKeyPair): String {
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val issuer = encodeIss(publicKey)
        val payload = IridiumJWTPayload(issuer, subject)
        val data = encodeData(JWT_IRIDIUM_HEADER, payload).encodeToByteArray()
        val signature = Ed25519Signer().run {
            init(true, privateKey)
            update(data, 0, data.size)
            generateSignature()
        }

        return encodeJWT(JWT_IRIDIUM_HEADER, payload, signature)
    }

    private fun encodeIss(publicKey: Ed25519PublicKeyParameters): String {
        val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey.encoded)

        return listOf(DID_PREFIX, DID_METHOD, multicodec).joinToString(DID_DELIMITER)
    }

    private fun encodeData(header: IridiumJWTHeader, payload: IridiumJWTPayload): String {
        return listOf(encodeJSON(header), encodeJSON(payload)).joinToString(JWT_DELIMITER)
    }

    private fun <T> encodeJSON(jsonObj: T): String {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
        val jsonByteArray = jsonString.toByteArray(Charsets.UTF_8)

        return jsonByteArray.joinToString(JWT_ENCODING)
    }

    private fun encodeJWT(header: IridiumJWTHeader, payload: IridiumJWTPayload, signature: ByteArray): String {
        return listOf(encodeJSON(header), encodeJSON(payload), encodeSig(signature)).joinToString(JWT_DELIMITER)
    }

    private fun encodeSig(signature: ByteArray): String {
        return Multibase.encode(Multibase.Base.Base64, signature)
    }

    @Test
    fun signJWTTest() {
        signJWT(nonce, keyPair).also { println(it.replace("base64url", "|").split("|")) }
    }

//    async function test() {
//        const jwt = await signJWT(nonce, keyPair);
//        console.log("jwt", jwt);
//        console.log("matches", jwt === expected);
//        const verified = await verifyJWT(jwt);
//        console.log("verified", verified);
//        const decoded = didJWT.decodeJWT(jwt);
//        console.log("decoded", decoded);
//        const keyDidResolver = KeyDIDResolver.getResolver();
//        const resolver = new Resolver(keyDidResolver);
//        const response = await didJWT.verifyJWT(jwt, { resolver });
//        console.log("response", response);
//    }

    private companion object {
        val JWT_IRIDIUM_HEADER = IridiumJWTHeader(algorithm = "EdDSA", type = "JWT")
        const val JWT_DELIMITER = "."
        const val JWT_ENCODING = "base64url"
        const val JSON_ENCODING = "utf8"
        const val DID_DELIMITER = ":"
        const val DID_PREFIX = "did"
        const val DID_METHOD = "key"
        const val MULTICODEC_ED25519_HEADER = "K36"
        const val MULTICODEC_ED25519_LENGTH = 32
    }
}