@file:JvmSynthetic
@file:JvmName("DidJwtRepository")

package com.walletconnect.android.internal.common.jwt.did

import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.*

fun <R : JwtClaims> encodeDidJwt(
    identityPrivateKey: PrivateKey,
    encodeDidJwtPayloadUseCase: EncodeDidJwtPayloadUseCase<R>,
    useCaseParams: EncodeDidJwtPayloadUseCase.Params,
): Result<DidJwt> = runCatching {
    val claims = encodeDidJwtPayloadUseCase(params = useCaseParams)
    val data = encodeData(JwtHeader.EdDSA.encoded, claims).toByteArray()
    val signature = signJwt(identityPrivateKey, data).getOrThrow()
    DidJwt(encodeJWT(JwtHeader.EdDSA.encoded, claims, signature))
}

inline fun <reified C : JwtClaims> extractVerifiedDidJwtClaims(didJwt: String): Result<C> = runCatching {
    val (header, claims, signature) = decodeJwt<C>(didJwt).getOrThrow()

    verifyHeader(header)
    verifyJwt(decodeEd25519DidKey(claims.issuer), extractData(didJwt).toByteArray(), signature)

    claims
}

fun verifyHeader(header: JwtHeader) {
    if (header.algorithm != JwtHeader.EdDSA.algorithm) throw Throwable("Unsupported header alg: ${header.algorithm}")
}

fun verifyJwt(identityPublicKey: PublicKey, data: ByteArray, signature: String) {
    val isValid = verifySignature(identityPublicKey, data, signature).getOrThrow()

    if (!isValid) throw Throwable("Invalid signature")
}

