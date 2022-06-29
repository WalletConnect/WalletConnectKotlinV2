package com.walletconnect.sign.network.data.service

import com.walletconnect.sign.network.model.NonceResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NonceService {

    @GET("auth-nonce")
    suspend fun authNonce(@Query("did") did: String): Response<NonceResponseDto>
}