package com.walletconnect.android.echo.network

import com.walletconnect.android.echo.model.EchoBody
import com.walletconnect.android.echo.model.EchoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface EchoService {

    @POST("{projectId}/clients")
    suspend fun register(@Path("projectId") projectId: String, @Body echoClientsBody: EchoBody): Response<EchoResponse>

    @DELETE("{projectId}/clients/{clientId}")
    suspend fun unregister(@Path("projectId") projectId: String, @Path("clientId") clientID: String): Response<EchoResponse>
}