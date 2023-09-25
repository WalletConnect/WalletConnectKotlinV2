@file:JvmSynthetic


package com.walletconnect.foundation.util.jwt

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.util.bytesToHex
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.net.URI
import java.util.concurrent.TimeUnit


fun encodeJWT(encodedHeader: String, claims: JwtClaims, signature: ByteArray): String {
    return listOf(encodedHeader, encodeJSON(claims), encodeBase64(signature)).joinToString(JWT_DELIMITER)
}

fun encodeData(encodedHeader: String, claims: JwtClaims): String {
    return listOf(encodedHeader, encodeJSON(claims)).joinToString(JWT_DELIMITER)
}

fun <T> encodeJSON(jsonObj: T): String {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val jsonString = moshi.adapter<T>(jsonObj!!::class.java).toJson(jsonObj)
    val jsonByteArray = jsonString.toByteArray()

    return encodeBase64(jsonByteArray)
}

@Suppress("NewApi")
fun encodeBase64(bytes: ByteArray): String {
    return String(io.ipfs.multibase.binary.Base64.encodeBase64URLSafe(bytes))
}

@Suppress("NewApi")
fun decodeBase64(value: ByteArray): String {
    return String(String(io.ipfs.multibase.binary.Base64.decodeBase64(value), Charsets.ISO_8859_1).toByteArray())
}

fun encodeEd25519DidKey(publicKey: ByteArray): String {
    val header: ByteArray = Base58.decode(MULTICODEC_ED25519_HEADER)
    val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

    return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
}

fun encodeX25519DidKey(publicKey: ByteArray): String {
    val header: ByteArray = Base58.decode(MULTICODEC_X25519_HEADER)
    val multicodec = Multibase.encode(Multibase.Base.Base58BTC, header + publicKey)

    return listOf(DID_PREFIX, DID_METHOD_KEY, multicodec).joinToString(DID_DELIMITER)
}

fun decodeEd25519DidKey(didKey: String): PublicKey {
    val decodedKey = Multibase.decode(didKey.split(DID_DELIMITER).last()).bytesToHex()
    if (!decodedKey.startsWith("ed01")) throw Throwable("Invalid key: $decodedKey")
    return PublicKey(decodedKey.removePrefix("ed01"))
}

fun decodeX25519DidKey(didKey: String): PublicKey {
    val decodedKey = Multibase.decode(didKey.split(DID_DELIMITER).last()).bytesToHex()
    if (!decodedKey.startsWith("ec01")) throw Throwable("Invalid key: $decodedKey")
    return PublicKey(decodedKey.removePrefix("ec01"))
}

fun jwtIatAndExp(timeunit: TimeUnit, expirySourceDuration: Long, expiryTimeUnit: TimeUnit, timestampInMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
    val iat = timeunit.convert(timestampInMs, TimeUnit.MILLISECONDS)
    val exp = iat + timeunit.convert(expirySourceDuration, expiryTimeUnit)
    return iat to exp
}

fun encodeDidPkh(caip10Account: String): String {
    return listOf(DID_PREFIX, DID_METHOD_PKH, caip10Account).joinToString(DID_DELIMITER)
}

fun decodeDidPkh(didPkh: String): String = didPkh.split(DID_DELIMITER).takeLast(3).joinToString(DID_DELIMITER)


fun encodeDidWeb(appDomain: String): String {
    val host = URI(appDomain).host
    return listOf(DID_PREFIX, DID_METHOD_WEB, host).joinToString(DID_DELIMITER)
}

// todo: What about https:// ?
fun decodeDidWeb(didWeb: String): String =
    didWeb.removePrefix(listOf(DID_PREFIX, DID_METHOD_WEB).joinToString(DID_DELIMITER))


inline fun <reified C : JwtClaims> decodeJwt(jwt: String): Result<Triple<JwtHeader, C, String>> = runCatching {
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

fun extractData(jwt: String): String {
    val (headerString, claimsString, _) = jwt.split(JWT_DELIMITER).also { if (it.size != 3) throw Throwable("Unable to split jwt: $jwt") }

    return listOf(headerString, claimsString).joinToString(JWT_DELIMITER)
}

fun signJwt(privateKey: PrivateKey, data: ByteArray): Result<ByteArray> = runCatching {
    val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.keyAsBytes)

    Ed25519Signer().run {
        init(true, privateKeyParameters)
        update(data, 0, data.size)
        generateSignature()
    }
}

fun verifySignature(publicKey: PublicKey, data: ByteArray, signature: String): Result<Boolean> = runCatching {
    val publicKeyParameters = Ed25519PublicKeyParameters(publicKey.keyAsBytes)

    Ed25519Signer().run {
        init(false, publicKeyParameters)
        update(data, 0, data.size)
        verifySignature(signature.toByteArray(Charsets.ISO_8859_1))
    }
}

const val JWT_DELIMITER = "."
const val DID_DELIMITER = ":"
const val DID_PREFIX = "did"
const val DID_METHOD_KEY = "key"
const val DID_METHOD_PKH = "pkh"
const val DID_METHOD_WEB = "web"
const val MULTICODEC_ED25519_HEADER = "K36"
const val MULTICODEC_X25519_HEADER = "Jxg"
