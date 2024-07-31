package com.walletconnect.android.verify.client

import android.util.Base64
import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.data.model.AttestationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

internal class VerifyRepository(
    private val verifyService: VerifyService,
    private val moshi: Moshi,
    private val keyManagementRepository: KeyManagementRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    fun resolve(attestationId: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                try {
                    val response = verifyService.resolveAttestation(attestationId)
                    if (response.isSuccessful && response.body() != null) {
                        val origin = response.body()!!.origin
                        val isScam = response.body()!!.isScam
                        onSuccess(AttestationResult(origin, isScam))
                    } else {
                        onError(IllegalArgumentException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }

    fun getVerifyPublicKey() {
        scope.launch {
            supervisorScope {
                try {

                    //todo: get key from DB + check expiry
                    val response = verifyService.getPublicKey()
                    if (response.isSuccessful) {
                        moshi.adapter(JWK::class.java).fromJson(response.body()!!.publicKey).let { jwk ->
                            if (jwk != null) {
                                println("kobe: JWK: $jwk")
                                //todo: jwk repository
                                val crv = jwk.crv
                                val xBytes: ByteArray = Base64.decode(jwk.x.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                                val yBytes: ByteArray = Base64.decode(jwk.y.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                                val point = ECPoint(xBytes.toBigInt(), yBytes.toBigInt())
                                val curveTableSpecs = ECNamedCurveTable.getParameterSpec(crv)
                                val ecSpec = ECNamedCurveSpec(crv, curveTableSpecs.curve, curveTableSpecs.g, curveTableSpecs.n)
                                val pubSpec = ECPublicKeySpec(point, ecSpec)
                                val kf = KeyFactory.getInstance("EC")
                                val publicKey: PublicKey = kf.generatePublic(pubSpec)
                                //todo: store key to DB: bytes, expiration
                            } else {
                                println("kobe: Error: Failed to parse JWK")
                            }
                        }
                    } else {
                        println("kobe: Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    println("kobe: Exception: $e")
                }
            }
        }
    }

    private companion object {
        fun ByteArray.toBigInt(): java.math.BigInteger {
            return java.math.BigInteger(1, this)
        }
    }
}