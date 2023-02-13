package com.walletconnect.push.dapp.data.network

import com.walletconnect.push.dapp.data.network.model.CastBodyDTO
import com.walletconnect.push.dapp.data.network.model.CastResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CastService {

    @POST("{projectId}/register")
    suspend fun register(@Path("projectId") projectId: String, @Body registerBody: CastBodyDTO.Register): Response<String>

    @POST("{projectId}/notify")
    suspend fun notify(@Path("projectId") projectId: String, @Body notify: CastBodyDTO.Notify): Response<CastResponseDTO.Notify>
}