package com.walletconnect.android.verify.domain

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.model.VerifyClaims
import com.walletconnect.util.hexToBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class VerifyRepository(
    private val verifyService: VerifyService,
    private val jwtRepository: JWTRepository,
    private val moshi: Moshi,
    private val verifyPublicKeyStorageRepository: VerifyPublicKeyStorageRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val mutex = Mutex()
    suspend fun getVerifyPublicKey(): Result<String> = mutex.withLock {
        runCatching {
            val (localPublicKey, expiresAt) = verifyPublicKeyStorageRepository.getPublicKey()
            val publicKey = if (!isLocalKeyValid(localPublicKey, expiresAt)) {
                fetchAndCacheKey()
            } else {
                localPublicKey!!
            }
            publicKey
        }
    }

    fun resolveV2(attestationJWT: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                getVerifyPublicKey().fold(
                    onSuccess = { key ->
                        if (jwtRepository.verifyJWT(attestationJWT, key.hexToBytes())) {
                            try {
                                val claims = moshi.adapter(VerifyClaims::class.java).fromJson(jwtRepository.decodeClaimsJWT(attestationJWT))
                                if (claims == null) {
                                    onError(IllegalArgumentException("Error while decoding JWT claims"))
                                    return@supervisorScope
                                }

                                onSuccess(VerifyResult(getValidation(claims, metadataUrl), claims.isScam, claims.origin))
                            } catch (e: Exception) {
                                onError(e)
                            }
                        } else {
                            try {
                                val newKey = fetchAndCacheKey()
                                if (jwtRepository.verifyJWT(attestationJWT, newKey.hexToBytes())) {
                                    val claims = moshi.adapter(VerifyClaims::class.java).fromJson(jwtRepository.decodeClaimsJWT(attestationJWT))
                                    if (claims == null) {
                                        onError(IllegalArgumentException("Error while decoding JWT claims"))
                                        return@supervisorScope
                                    }

                                    onSuccess(VerifyResult(getValidation(claims, metadataUrl), claims.isScam, claims.origin))
                                } else {
                                    onError(IllegalArgumentException("Error while verifying JWT"))
                                }
                            } catch (e: Exception) {
                                onError(e)
                            }
                        }
                    },
                    onFailure = { error ->
                        onError(error)
                    }
                )
            }
        }
    }

    private fun getValidation(claims: VerifyClaims, metadataUrl: String): Validation =
        when {
            !claims.isVerified || currentTimeInSeconds >= claims.expiration -> Validation.UNKNOWN
            else -> getValidation(metadataUrl, claims.origin)
        }

    private suspend fun fetchAndCacheKey(): String {
        val response = verifyService.getPublicKey()
        if (response.isSuccessful && response.body() != null) {
            val publicKey = jwtRepository.generateP256PublicKeyFromJWK(response.body()!!.jwk)
            verifyPublicKeyStorageRepository.upsertPublicKey(publicKey, response.body()!!.expiresAt)
            return publicKey
        } else {
            throw Exception("Error while fetching a Verify PublicKey: ${response.errorBody()?.string()}")
        }
    }

    fun resolve(attestationId: String, metadataUrl: String, onSuccess: (VerifyResult) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                try {
                    val response = verifyService.resolveAttestation(attestationId)
                    if (response.isSuccessful && response.body() != null) {
                        val origin = response.body()!!.origin
                        val isScam = response.body()!!.isScam

                        onSuccess(VerifyResult(getValidation(metadataUrl, origin), isScam, origin))
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