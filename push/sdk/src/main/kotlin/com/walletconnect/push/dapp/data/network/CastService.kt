package com.walletconnect.push.dapp.data.network

import com.walletconnect.push.dapp.data.network.model.CastBody
import com.walletconnect.push.dapp.data.network.model.CastResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CastService {

    @POST("{projectId}/register")
    suspend fun register(@Path("projectId") projectId: String, @Body registerBody: CastBody.Register): Response<String>

    @POST("{projectId}/notify")
    suspend fun notify(@Path("projectId") projectId: String, @Body notify: CastBody.Notify): Response<CastResponse.Notify>
}