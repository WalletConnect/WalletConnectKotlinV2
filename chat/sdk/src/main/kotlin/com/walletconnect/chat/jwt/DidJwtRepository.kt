@file:JvmSynthetic

package com.walletconnect.chat.jwt

import com.walletconnect.foundation.util.jwt.*
import com.walletconnect.chat.jwt.use_case.EncodeDidJwtPayloadUseCase
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey

internal class DidJwtRepository {
    fun encodeDidJwt(identityKeyPair: Pair<PublicKey, PrivateKey>, keyserverUrl: String, encodeDidJwtPayloadUseCase: EncodeDidJwtPayloadUseCase): Result<String> = runCatching {
        val (publicKey, privateKey) = identityKeyPair

        val issuer = encodeEd25519DidKey(publicKey.keyAsBytes)
        val issuedAt = jwtIat()
        val expiration = jwtExp(issuedAt)
        val claims = encodeDidJwtPayloadUseCase(issuer = issuer, keyserverUrl = keyserverUrl, issuedAt = issuedAt, expiration = expiration)
        val data = encodeData(JwtHeader.EdDSA.encoded, claims).toByteArray()
        val signature = signJwt(privateKey, data).getOrThrow()

        encodeJWT(JwtHeader.EdDSA.encoded, claims, signature)
    }

    internal inline fun <reified C : ChatDidJwtClaims> extractVerifiedDidJwtClaims(didJwt: String): Result<C> = runCatching {
        val (header, claims, signature) = decodeJwt<C>(didJwt).getOrThrow()

        verifyHeader(header)
        verifyJwt(decodeEd25519DidKey(claims.issuer), extractData(didJwt).toByteArray(), signature)

        claims
    }

    private fun verifyHeader(header: JwtHeader) {
        if (header.algorithm != JwtHeader.EdDSA.algorithm) throw Throwable("Unsupported header alg: ${header.algorithm}")
    }

    private fun verifyJwt(identityPublicKey: PublicKey, data: ByteArray, signature: String) {
        val isValid = verifySignature(identityPublicKey, data, signature).getOrThrow()

        if (!isValid) throw Throwable("Invalid signature")
    }
}

