package com.walletconnect.android.verify.domain

import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.util.hexToBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class VerifyRepository(
    private val verifyService: VerifyService,
    private val jwtRepository: JWTRepository,
    private val verifyPublicKeyStorageRepository: VerifyPublicKeyStorageRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    suspend fun getVerifyPublicKey(): Result<String> {
        return kotlin.runCatching {
            val (localPublicKey, expiresAt) = verifyPublicKeyStorageRepository.getPublicKey()
            val publicKey = if (!isLocalKeyValid(localPublicKey, expiresAt)) {
                fetchAndCacheKey()
            } else {
                println("kobe: Local key is valid: $localPublicKey")
                localPublicKey!!
            }
            publicKey
        }
    }

    fun resolveV2(attestation: String, onSuccess: (AttestationResult) -> Unit, onError: (Throwable) -> Unit) {
        println("kobe: Verify v2: attestation jwt: $attestation")

        scope.launch {
            supervisorScope {
                getVerifyPublicKey()
                    .fold(
                        onSuccess = { key ->
                            println("kobe: Gen key2: ${key}")
                            val (header, claims, signature) = jwtRepository.decodeJWT(attestation)
                            //varify claims
                            val data = "$header.$claims".toByteArray()

                            val isValid = jwtRepository.verifyJWT(data, signature, key.hexToBytes())
                            println("kobe: isValid: $isValid")
                            //if verification fails get new key and try again
                            //return onSuccess

                        },
                        onFailure = { throwable -> onError(throwable) }
                    )
            }
        }
    }

    private suspend fun fetchAndCacheKey(): String {
        val response = verifyService.getPublicKey()
        if (response.isSuccessful && response.body() != null) {
            println("kobe: JWK: ${response.body()!!.jwk}")
            val publicKey = jwtRepository.generateP256PublicKeyFromJWK(response.body()!!.jwk)
            verifyPublicKeyStorageRepository.upsertPublicKey(publicKey, response.body()!!.expiresAt)
            return publicKey
        } else {
            println("kobe: Error while fetching a key: ${response.errorBody()?.string()}")
            throw Exception("Error while fetching a Verify PublicKey: ${response.errorBody()?.string()}")
        }
    }

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

    private fun isLocalKeyValid(localPublicKey: String?, expiresAt: Long?) = localPublicKey != null && expiresAt != null && !isKeyExpired(expiresAt)
    private fun isKeyExpired(expiresAt: Long): Boolean = currentTimeInSeconds >= expiresAt
}