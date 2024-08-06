package com.walletconnect.android.internal

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.domain.JWTRepository
import com.walletconnect.android.verify.domain.VerifyPublicKeyStorageRepository
import com.walletconnect.android.verify.domain.VerifyRepository
import com.walletconnect.android.verify.domain.VerifyResult
import com.walletconnect.android.verify.model.JWK
import com.walletconnect.android.verify.model.Origin
import com.walletconnect.android.verify.model.VerifyServerPublicKey
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class VerifyRepositoryTest {
    private val verifyService = mockk<VerifyService>(relaxed = true)
    private val jwtRepository = mockk<JWTRepository>(relaxed = true)
    private val moshi = Moshi.Builder().build()
    private val verifyPublicKeyStorageRepository = mockk<VerifyPublicKeyStorageRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private var verifyRepository: VerifyRepository = VerifyRepository(
        verifyService,
        jwtRepository,
        moshi,
        verifyPublicKeyStorageRepository,
        testScope
    )
    private val jwk = JWK(
        kty = "EC",
        crv = "P-256",
        x = "CbL4DOYOb1ntd-8OmExO-oS0DWCMC00DntrymJoB8tk",
        y = "KTFwjHtQxGTDR91VsOypcdBfvbo6sAMj5p4Wb-9hRA0",
        keyOps = listOf("verify"),
        ext = true
    )

    @Test
    fun `getVerifyPublicKey returns cached key when valid`() = testScope.runTest {
        val localKey = "localPublicKey"
        val expiresAt = currentTimeInSeconds + 1000
        coEvery { verifyPublicKeyStorageRepository.getPublicKey() } returns Pair(localKey, expiresAt)

        val result = verifyRepository.getVerifyPublicKey()

        assertEquals(Result.success(localKey), result)
    }

    @Test
    fun `getVerifyPublicKey fetches and caches new key when local key is invalid`() = testScope.runTest {
        val newKey = "newPublicKey"
        coEvery { verifyPublicKeyStorageRepository.getPublicKey() } returns Pair(null, null)
        coEvery { verifyService.getPublicKey() } returns Response.success(VerifyServerPublicKey(jwk, expiresAt = currentTimeInSeconds + 1000))
        coEvery { jwtRepository.generateP256PublicKeyFromJWK(any()) } returns newKey
        coEvery { verifyPublicKeyStorageRepository.upsertPublicKey(any(), any()) } just Runs

        val result = verifyRepository.getVerifyPublicKey()

        assertEquals(Result.success(newKey), result)
        coVerify { verifyPublicKeyStorageRepository.upsertPublicKey(newKey, any()) }
    }

    @Test
    fun `resolveV2 calls onSuccess when JWT is valid`() = testScope.runTest {
        val attestationJWT = "attestationJWT"
        val metadataUrl = "https://metadata.url"
        val publicKey = "0409b2f80ce60e6f59ed77ef0e984c4efa84b40d608c0b4d039edaf2989a01f2d92931708c7b50c464c347dd55b0eca971d05fbdba3ab00323e69e166fef61440d"
        val verifyResult = VerifyResult(Validation.UNKNOWN, null, "https://react-dapp-v2-git-chore-verify-v2-samples-walletconnect1.vercel.app")
        val claimsJson =
            """{"exp":1722579908,"id":"5106a25552e89acfb5bed83ee21bf4e80dbcd51b0b203f6925a369aacb1c860b","origin":"https://react-dapp-v2-git-chore-verify-v2-samples-walletconnect1.vercel.app","isScam":null,"isVerified":true}"""

        coEvery { verifyPublicKeyStorageRepository.getPublicKey() } returns Pair(publicKey, currentTimeInSeconds + 1000)
        every { jwtRepository.verifyJWT(any(), any()) } returns true
        every { jwtRepository.decodeClaimsJWT(any()) } returns claimsJson

        val onSuccess = mockk<(VerifyResult) -> Unit>(relaxed = true)
        val onError = mockk<(Throwable) -> Unit>(relaxed = true)

        verifyRepository.resolveV2(attestationJWT, metadataUrl, onSuccess, onError)

        advanceUntilIdle()

        verify { onSuccess(verifyResult) }
    }

    @Test
    fun `resolve calls onSuccess when service resolves attestation successfully`() = testScope.runTest {
        val attestationId = "attestationId"
        val metadataUrl = "https://metadata.url"
        val response = mockk<Origin>(relaxed = true) {
            every { origin } returns "origin"
            every { isScam } returns false
        }

        coEvery { verifyService.resolveAttestation(any()) } returns Response.success(response)

        val onSuccess = mockk<(VerifyResult) -> Unit>(relaxed = true)
        val onError = mockk<(Throwable) -> Unit>(relaxed = true)

        verifyRepository.resolve(attestationId, metadataUrl, onSuccess, onError)

        advanceUntilIdle()

        verify { onSuccess(VerifyResult(Validation.INVALID, false, "origin")) }
    }

    @Test
    fun `resolve calls onError when service fails to resolve attestation`() = testScope.runTest {
        val attestationId = "attestationId"
        val metadataUrl = "https://metadata.url"
        val errorMessage = "error"

        coEvery { verifyService.resolveAttestation(any()) } returns Response.error(400, errorMessage.toResponseBody())

        val onSuccess = mockk<(VerifyResult) -> Unit>(relaxed = true)
        val onError = mockk<(Throwable) -> Unit>(relaxed = true)

        verifyRepository.resolve(attestationId, metadataUrl, onSuccess, onError)

        advanceUntilIdle()

        verify {
            onError(any<IllegalArgumentException>())
        }
    }
}