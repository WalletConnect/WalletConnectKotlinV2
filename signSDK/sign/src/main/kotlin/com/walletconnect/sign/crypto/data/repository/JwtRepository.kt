package com.walletconnect.sign.crypto.data.repository

import android.content.SharedPreferences
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.core.model.vo.PrivateKey
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.sign.crypto.data.repository.model.IridiumJWTHeader
import com.walletconnect.sign.crypto.data.repository.model.IridiumJWTPayload
import com.walletconnect.sign.network.data.service.NonceService
import com.walletconnect.sign.network.model.NonceResponseDto
import com.walletconnect.sign.util.Empty
import com.walletconnect.sign.util.Logger
import com.walletconnect.sign.util.bytesToHex
import com.walletconnect.sign.util.hexToBytes
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import retrofit2.Response
import java.security.SecureRandom

internal class JwtRepository(private val sharedPreferences: SharedPreferences, private val keyChain: KeyStore, private val nonceService: NonceService) {

    fun jwtExists(): Boolean {
        return sharedPreferences.contains(KEY_JWT)
    }

    fun getJWT(): String {
        return sharedPreferences.getString(KEY_JWT, null)!!
    }

    fun signJWT(subject: String): String {
        val (privateKey, publicKey) = keyChain.getKeys(KEY_DID_KEYPAIR)
        val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.hexToBytes())

        val issuer = encodeIss(publicKey.hexToBytes())
        val payload = IridiumJWTPayload(issuer, subject)
        val data = encodeData(JWT_IRIDIUM_HEADER, payload).encodeToByteArray()
        val signature = Ed25519Signer().run {
            init(true, privateKeyParameters)
            update(data, 0, data.size)
            generateSignature()
        }

        return encodeJWT(JWT_IRIDIUM_HEADER, payload, signature)
    }

    suspend fun getNonceFromNewDID(): String? {
        val did = getDIDFromNewKeyPair()
        val response: Response<NonceResponseDto> = nonceService.authNonce(did)

        return if (response.isSuccessful) {
            response.body()?.nonce
        } else {
            Logger.error(response.message())
            null
        }
    }

    private fun encodeIss(publicKey: ByteArray): String {
        val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

        return listOf(DID_PREFIX, DID_METHOD, multicodec).joinToString(DID_DELIMITER)
    }

    private fun encodeData(header: IridiumJWTHeader, payload: IridiumJWTPayload): String {
        return listOf(encodeJSON(header), encodeJSON(payload)).joinToString(JWT_DELIMITER)
    }

    private fun encodeJWT(header: IridiumJWTHeader, payload: IridiumJWTPayload, signature: ByteArray): String {
        return listOf(encodeJSON(header), encodeJSON(payload), encodeByteArray(signature)).joinToString(JWT_DELIMITER)
    }

    private fun <T> encodeJSON(jsonObj: T): String {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
        val jsonByteArray = jsonString.toByteArray(Charsets.UTF_8)

        return encodeByteArray(jsonByteArray)
    }

    private fun encodeByteArray(signature: ByteArray): String {
        return Base64.encodeToString(signature, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun getDIDFromNewKeyPair(): String {
        val secureRandom = SecureRandom(ByteArray(KEY_SIZE))
        val keyPair: AsymmetricCipherKeyPair = Ed25519KeyPairGenerator().run {
            this.init(Ed25519KeyGenerationParameters(secureRandom))
            this.generateKeyPair()
        }
        val publicKeyParameters = keyPair.public as Ed25519PublicKeyParameters
        val privateKeyParameters = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = PublicKey(publicKeyParameters.encoded.bytesToHex())
        val privateKey = PrivateKey(privateKeyParameters.encoded.bytesToHex())

        keyChain.setKeys(KEY_DID_KEYPAIR, publicKey, privateKey)

        return publicKey.keyAsHex
    }

    private companion object {
        const val KEY_DID_KEYPAIR = "key_did_keypair"
        const val KEY_JWT = "key_jwt"
        val JWT_IRIDIUM_HEADER = IridiumJWTHeader(algorithm = "EdDSA", type = "JWT")
        const val KEY_SIZE: Int = 32
        const val JWT_DELIMITER = "."
        const val DID_DELIMITER = ":"
        const val DID_PREFIX = "did"
        const val DID_METHOD = "key"
        const val MULTICODEC_ED25519_HEADER = "K36"
    }
}