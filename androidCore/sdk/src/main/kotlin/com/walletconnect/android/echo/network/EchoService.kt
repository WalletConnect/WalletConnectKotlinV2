package com.walletconnect.android.echo.network

import com.walletconnect.android.echo.model.EchoBody
import com.walletconnect.android.echo.model.EchoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface EchoService {

    @POST("893ad3af-9515-42d6-aaa2-06b53494b9a5/clients")
    suspend fun register(@Body echoClientsBody: EchoBody): Response<EchoResponse>

    @DELETE("893ad3af-9515-42d6-aaa2-06b53494b9a5/clients/{clientId}")
    suspend fun unregister(@Path("clientId") clientID: String): Response<EchoResponse>
}