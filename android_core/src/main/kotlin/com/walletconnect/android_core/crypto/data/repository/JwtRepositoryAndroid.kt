@file:JvmSynthetic

package com.walletconnect.android_core.crypto.data.repository

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.android_core.crypto.KeyStore
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.crypto.data.repository.BaseJwtRepository
import com.walletconnect.foundation.crypto.data.repository.model.IrnJWTHeader
import com.walletconnect.foundation.crypto.data.repository.model.IrnJWTPayload
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import com.walletconnect.util.randomBytes
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.sign.crypto.KeyStore
import com.walletconnect.sign.crypto.data.repository.model.IrnJWTHeader
import com.walletconnect.sign.crypto.data.repository.model.IrnJWTPayload
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import com.walletconnect.util.randomBytes
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

internal class JwtRepositoryAndroid(private val keyChain: KeyStore): BaseJwtRepository() {

    override fun generateJWT(serverUrl: String): String {
        val subject = generateSubject()
        val (publicKey, privateKey) = getKeyPair()
        val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.hexToBytes())

        val issuer = encodeIss(publicKey.hexToBytes())
        val issuedAt = TimeUnit.SECONDS.convert(getCurrentTimestamp(), TimeUnit.MILLISECONDS)
        val expiration = jwtExp(issuedAt)
        val payload = IrnJWTPayload(issuer, subject, serverUrl, issuedAt, expiration)
        val data = encodeData(JWT_IRN_HEADER, payload).encodeToByteArray()
        val signature = Ed25519Signer().run {
            init(true, privateKeyParameters)
            update(data, 0, data.size)
            generateSignature()
        }

        return encodeJWT(JWT_IRN_HEADER, payload, signature)
    }

    override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {
        TODO("Not yet implemented")
    }

    private fun encodeIss(publicKey: ByteArray): String {
        val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
        val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

        return listOf(DID_PREFIX, DID_METHOD, multicodec).joinToString(DID_DELIMITER)
    }

    private fun encodeData(header: IrnJWTHeader, payload: IrnJWTPayload): String {
        return listOf(encodeJSON(header), encodeJSON(payload)).joinToString(JWT_DELIMITER)
    }

    private fun encodeJWT(header: IrnJWTHeader, payload: IrnJWTPayload, signature: ByteArray): String {
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

    override fun encodeByteArray(signature: ByteArray): String {
        return Base64.encodeToString(signature, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    override fun getKeyPair(): Pair<String, String> {
        return if (doesKeyPairExist()) {
            val (privateKey, publicKey) = keyChain.getKeys(KEY_DID_KEYPAIR)
            publicKey to privateKey
        } else {
            getDIDFromNewKeyPair()
        }
    }

    private fun doesKeyPairExist(): Boolean {
        return keyChain.checkKeys(KEY_DID_KEYPAIR)
    }

    private fun getDIDFromNewKeyPair(): Pair<String, String> {
        val secureRandom = SecureRandom(ByteArray(KEY_SIZE))
        val keyPair: AsymmetricCipherKeyPair = Ed25519KeyPairGenerator().run {
            this.init(Ed25519KeyGenerationParameters(secureRandom))
            this.generateKeyPair()
        }
        val publicKeyParameters = keyPair.public as Ed25519PublicKeyParameters
        val privateKeyParameters = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = PublicKey(publicKeyParameters.encoded.bytesToHex())
        val privateKey = PrivateKey(privateKeyParameters.encoded.bytesToHex())

        keyChain.setKeys(KEY_DID_KEYPAIR, privateKey, publicKey)

        return publicKey.keyAsHex to privateKey.keyAsHex
    }

    internal fun generateSubject() = randomBytes(KEY_NONCE_SIZE).bytesToHex()

    private fun jwtExp(issuedAt: Long): Long {
        return issuedAt + TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
    }

    internal fun getCurrentTimestamp() = System.currentTimeMillis()

    private companion object {
        const val KEY_DID_KEYPAIR = "key_did_keypair"
        val JWT_IRN_HEADER = IrnJWTHeader(algorithm = "EdDSA", type = "JWT")
        const val KEY_SIZE: Int = 32
        const val KEY_NONCE_SIZE = KEY_SIZE
        const val JWT_DELIMITER = "."
        const val DID_DELIMITER = ":"
        const val DID_PREFIX = "did"
        const val DID_METHOD = "key"
        const val MULTICODEC_ED25519_HEADER = "K36"
    }
}