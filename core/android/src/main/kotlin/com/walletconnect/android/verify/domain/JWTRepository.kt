package com.walletconnect.android.verify.domain

import com.walletconnect.android.verify.model.JWK
import com.walletconnect.foundation.util.jwt.JWT_DELIMITER
import com.walletconnect.util.bytesToHex
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

class JWTRepository {

    fun generateP256PublicKeyFromJWK(jwk: JWK): String {
        val xBytes: ByteArray = io.ipfs.multibase.binary.Base64.decodeBase64(jwk.x)
        val yBytes: ByteArray = io.ipfs.multibase.binary.Base64.decodeBase64(jwk.y)
        val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("P-256")
        val ecPoint = ecSpec.curve.createPoint(xBytes.toBigInt(), yBytes.toBigInt())
        val domainParams = ECDomainParameters(ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h)
        val pubKeyParams = ECPublicKeyParameters(ecPoint, domainParams)
        val pubKeyBytes = pubKeyParams.q.getEncoded(false)
        println("kobe: Gen key: ${pubKeyBytes.bytesToHex()}")
        return pubKeyBytes.bytesToHex()
    }

    fun verifyJWT(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        val r = BigInteger(1, signature.sliceArray(0 until (signature.size / 2)))
        val s = BigInteger(1, signature.sliceArray((signature.size / 2) until signature.size))


        val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("P-256")
        val ecPoint: ECPoint = ecSpec.curve.decodePoint(publicKey)
        val domainParams = ECDomainParameters(ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h)
        val pubKeyParams = ECPublicKeyParameters(ecPoint, domainParams)

        val signer = ECDSASigner()
        signer.init(false, pubKeyParams)
        val sha256Digest = SHA256Digest()
        sha256Digest.update(data, 0, data.size)
        val hash = ByteArray(sha256Digest.digestSize)
        sha256Digest.doFinal(hash, 0)

        val isVerified = signer.verifySignature(hash, r, s)

        return isVerified
    }

    fun decodeJWT(jwt: String): Triple<String, String, ByteArray> {
        val (headerString, claimsString, signatureString) = jwt.split(JWT_DELIMITER).also { if (it.size != 3) throw Throwable("Unable to split jwt: $jwt") }

        println("kobe: JWT: header: $headerString, claims: $claimsString, signature: $signatureString")

        val headerDecoded = io.ipfs.multibase.binary.Base64.decodeBase64(headerString).toString(Charsets.UTF_8)
        val claimsDecoded = io.ipfs.multibase.binary.Base64.decodeBase64(claimsString).toString(Charsets.UTF_8)
        val signatureDecoded = io.ipfs.multibase.binary.Base64.decodeBase64(signatureString)

        println("kobe: JWT decoded: header: $headerDecoded, claims: $claimsDecoded, signature: $signatureDecoded")

        return Triple(headerString, claimsString, signatureDecoded)
    }

    private companion object {
        fun ByteArray.toBigInt(): BigInteger = BigInteger(1, this)
    }
}