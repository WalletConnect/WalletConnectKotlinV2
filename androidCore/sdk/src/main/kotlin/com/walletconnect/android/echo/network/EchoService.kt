package com.walletconnect.android.echo.network

import com.walletconnect.android.echo.network.model.EchoBody
import com.walletconnect.android.echo.network.model.EchoResponse
import retrofit2.Response
import retrofit2.http.*

interface EchoService {

    @POST("{projectId}/clients")
    suspend fun register(@Path("projectId") projectId: String, @Query("auth") clientID: String, @Body echoClientsBody: EchoBody): Response<EchoResponse>

    @DELETE("{projectId}/clients/{clientId}")
    suspend fun unregister(@Path("projectId") projectId: String, @Path("clientId") clientID: String): Response<EchoResponse>
}