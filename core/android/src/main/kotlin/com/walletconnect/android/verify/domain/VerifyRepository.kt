package com.walletconnect.android.verify.domain

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.model.JWK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class VerifyRepository(
    private val verifyService: VerifyService,
    private val moshi: Moshi,
    private val keyManagementRepository: KeyManagementRepository,
    private val verifyPublicKeyStorageRepository: VerifyPublicKeyStorageRepository,
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
                    val (localPublicKey, expiresAt) = verifyPublicKeyStorageRepository.getPublicKey()
                    if (!shouldFetchKey(localPublicKey, expiresAt)) {
                        val response = verifyService.getPublicKey()
                        if (response.isSuccessful && response.body() != null) {
                            moshi.adapter(JWK::class.java).fromJson(response.body()!!.publicKey).let { jwk ->
                                if (jwk != null) {
                                    println("kobe: JWK: $jwk")
                                    val publicKey = keyManagementRepository.generateP256PublicKeyFromJWK(jwk)
                                    verifyPublicKeyStorageRepository.upsertPublicKey(publicKey, response.body()!!.expiresAt)
                                } else {
                                    println("kobe: Error: Failed to parse JWK")
                                }
                            }
                        } else {
                            println("kobe: Error: ${response.errorBody()?.string()}")
                        }
                    } else {
                        println("kobe: Key is not expired: $localPublicKey")
                    }
                } catch (e: Exception) {
                    println("kobe: Exception: $e")
                }
            }
        }
    }

    private fun shouldFetchKey(localPublicKey: String?, expiresAt: Long?) = localPublicKey != null && expiresAt != null && isKeyExpired(expiresAt)
    private fun isKeyExpired(expiresAt: Long): Boolean = System.currentTimeMillis() >= expiresAt
}