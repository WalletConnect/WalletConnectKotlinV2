package com.walletconnect.android.verify.data

import com.walletconnect.android.verify.model.Origin
import com.walletconnect.android.verify.model.RegisterAttestationBody
import com.walletconnect.android.verify.model.VerifyServerResponse
import retrofit2.Response
import retrofit2.http.*

interface VerifyService {
    @Headers("Content-Type: application/json")
    @POST("attestation")
    suspend fun registerAttestation(@Body body: RegisterAttestationBody): Response<VerifyServerResponse.RegisterAttestation>

    @Headers("Content-Type: application/json")
    @GET("attestation/{attestationId}")
    suspend fun resolveAttestation(@Path("attestationId") attestationId: String): Response<Origin>
}