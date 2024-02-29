@file:JvmSynthetic

package com.walletconnect.android.pulse

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PulseService {

    @Headers("Content-Type: application/json")
    @POST("/e")
    suspend fun sendEvent(@Header("x-sdk-type") sdkType: String): Response<PulseResponse>
}