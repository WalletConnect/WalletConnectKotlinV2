package com.walletconnect.web3.modal.data.network

import com.walletconnect.web3.modal.data.model.IdentityDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface BlockchainService {

    @GET("identity/{address}")
    suspend fun getIdentity(
        @Path("address") address: String,
        @Query("chainId") chainId: String,
        @Query("projectId") projectId: String
    ): Response<IdentityDTO>

}
