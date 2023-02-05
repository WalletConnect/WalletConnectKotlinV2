@file:JvmSynthetic


package com.walletconnect.chat.authentication.jwt

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.util.bytesToHex
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.util.concurrent.TimeUnit


internal fun encodeJWT(encodedHeader: String, claims: JwtClaims, signature: ByteArray): String {
    return listOf(encodedHeader, encodeJSON(claims), encodeBase64(signature)).joinToString(JWT_DELIMITER)
}

internal fun encodeData(encodedHeader: String, claims: JwtClaims): String {
    return listOf(encodedHeader, encodeJSON(claims)).joinToString(JWT_DELIMITER)
}

internal fun <T> encodeJSON(jsonObj: T): String {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
    val jsonByteArray = jsonString.toByteArray()

    return encodeBase64(jsonByteArray)
}

@Suppress("NewApi")
internal fun encodeBase64(bytes: ByteArray): String {
    return String(java.util.Base64.getUrlEncoder().withoutPadding().encode(bytes))
}


@Suppress("NewApi")
internal fun decodeBase64(value: ByteArray): String {
    return String(String(java.util.Base64.getUrlDecoder().decode(value), Charsets.ISO_8859_1).toByteArray())
}

internal fun encodeEd25519DidKey(publicKey: ByteArray): String {
    val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
    val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

    return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
}

internal fun encodeX25519DidKey(publicKey: ByteArray): String {
    val header: ByteArray = Base58.decode(MULTICODEC_X25519_HEADER)
    val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

    return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
}

internal fun decodeEd25519DidKey(didKey: String): PublicKey {
    val decodedKey = Multibase.decode(didKey.split(DID_DELIMITER).last()).bytesToHex()
    if (!decodedKey.startsWith("ed01")) throw Throwable("Invalid key: $decodedKey")
    return PublicKey(decodedKey.removePrefix("ed01"))
}

internal fun decodeX25519DidKey(didKey: String): PublicKey {
    val decodedKey = Multibase.decode(didKey.split(DID_DELIMITER).last()).bytesToHex()
    if (!decodedKey.startsWith("ec01")) throw Throwable("Invalid key: $decodedKey")
    return PublicKey(decodedKey.removePrefix("ec01"))
}

internal fun jwtIat(): Long = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

internal fun jwtExp(issuedAt: Long): Long {
    return issuedAt + TimeUnit.SECONDS.convert(1, TimeUnit.DAYS)
}

internal fun encodeDidPkh(accountId: AccountId): String {
    return listOf(DID_PREFIX, DID_METHOD_PKH, accountId.value).joinToString(DID_DELIMITER)
}

internal fun decodeDidPkh(didPkh: String): AccountId = AccountId(didPkh.split(DID_DELIMITER).takeLast(3).joinToString(DID_DELIMITER))
    .takeIf { it.isValid() } ?: throw Throwable("Invalid did:pkh :$didPkh")

internal inline fun <reified C : JwtClaims> decodeJwt(jwt: String): Result<Triple<JwtHeader, C, String>> = runCatching {
    val (headerString, claimsString, signatureString) = jwt.split(JWT_DELIMITER).also { if (it.size != 3) throw Throwable("Unable to split jwt: $jwt") }

    val claimsDecoded = decodeBase64(claimsString.toByteArray())
    val headerDecoded = decodeBase64(headerString.toByteArray())
    val signatureDecoded = decodeBase64(signatureString.toByteArray())

    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val claims = moshi.adapter(C::class.java).fromJson(claimsDecoded) ?: throw Throwable("Invalid claims: $claimsString")
    val header = moshi.adapter(JwtHeader::class.java).fromJson(headerDecoded) ?: throw Throwable("Invalid header: $headerString")

    Triple(header, claims, signatureDecoded)
}

internal fun extractData(jwt: String): String {
    val (headerString, claimsString, _) = jwt.split(JWT_DELIMITER).also { if (it.size != 3) throw Throwable("Unable to split jwt: $jwt") }

    return listOf(headerString, claimsString).joinToString(JWT_DELIMITER)
}

internal fun signJwt(privateKey: PrivateKey, data: ByteArray): Result<ByteArray> = runCatching {
    val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.keyAsBytes)

    Ed25519Signer().run {
        init(true, privateKeyParameters)
        update(data, 0, data.size)
        generateSignature()
    }
}

internal fun verifySignature(publicKey: PublicKey, data: ByteArray, signature: String): Result<Boolean> = runCatching {
    val publicKeyParameters = Ed25519PublicKeyParameters(publicKey.keyAsBytes)

    Ed25519Signer().run {
        init(false, publicKeyParameters)
        update(data, 0, data.size)
        verifySignature(signature.toByteArray(Charsets.ISO_8859_1))
    }
}

internal const val JWT_DELIMITER = "."
internal const val DID_DELIMITER = ":"
internal const val DID_PREFIX = "did"
internal const val DID_METHOD_KEY = "key"
internal const val DID_METHOD_PKH = "pkh"
internal const val MULTICODEC_ED25519_HEADER = "K36"
internal const val MULTICODEC_X25519_HEADER = "Jxg"
