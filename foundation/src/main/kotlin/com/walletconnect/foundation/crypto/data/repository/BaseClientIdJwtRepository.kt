package com.walletconnect.foundation.crypto.data.repository

import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.crypto.data.repository.model.IrnJwtClaims
import com.walletconnect.foundation.util.jwt.*
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import com.walletconnect.util.randomBytes
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

abstract class BaseClientIdJwtRepository : JwtRepository {
    abstract fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey)

    open fun getKeyPair(): Pair<String, String> {
        return generateAndStoreClientIdKeyPair()
    }

    override fun generateJWT(serverUrl: String, getIssuerClientId: (String) -> Unit): String {
        val subject = generateSubject()
        val (publicKey, privateKey) = getKeyPair()

        val issuer = encodeEd25519DidKey(publicKey.hexToBytes())
        val clientId = issuer.split(":").last()
        getIssuerClientId(clientId)
        // ClientId Did Jwt have issuedAt as seconds
        val (issuedAt, expiration) = jwtIatAndExp(timeunit = TimeUnit.SECONDS, expirySourceDuration = 1, expiryTimeUnit = TimeUnit.DAYS)
        val payload = IrnJwtClaims(issuer, subject, serverUrl, issuedAt, expiration)
        val data = encodeData(JwtHeader.EdDSA.encoded, payload).toByteArray()
        val signature = signJwt(PrivateKey(privateKey), data).getOrThrow()

        return encodeJWT(JwtHeader.EdDSA.encoded, payload, signature)
    }

    fun generateAndStoreClientIdKeyPair(): Pair<String, String> {
        val secureRandom = SecureRandom(ByteArray(KEY_SIZE))
        val keyPair: AsymmetricCipherKeyPair = Ed25519KeyPairGenerator().run {
            this.init(Ed25519KeyGenerationParameters(secureRandom))
            this.generateKeyPair()
        }
        val publicKeyParameters = keyPair.public as Ed25519PublicKeyParameters
        val privateKeyParameters = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = PublicKey(publicKeyParameters.encoded.bytesToHex())
        val privateKey = PrivateKey(privateKeyParameters.encoded.bytesToHex())

        setKeyPair(CLIENT_ID_KEYPAIR_TAG, privateKey, publicKey)

        return publicKey.keyAsHex to privateKey.keyAsHex
    }

    fun generateSubject() = randomBytes(KEY_NONCE_SIZE).bytesToHex()

    companion object {
        const val CLIENT_ID_KEYPAIR_TAG = "key_did_keypair" // Has to stay with this value to be backwards compatible
        private const val KEY_SIZE: Int = 32
        private const val KEY_NONCE_SIZE = KEY_SIZE
    }
}