package com.walletconnect.android.archive.network

import com.walletconnect.android.archive.network.model.messages.MessagesResponse
import com.walletconnect.android.archive.network.model.register.RegisterBody
import com.walletconnect.android.archive.network.model.register.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap


interface HistoryServerService {

    @POST("/register")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body body: RegisterBody, @Header("Authorization") authorization: String): Response<RegisterResponse>

    @GET("/messages")
    @Headers("Content-Type: application/json")
    suspend fun messages(@QueryMap queryMap: Map<String, String>): Response<MessagesResponse>
}