package com.walletconnect.web3.modal.data.network

import com.walletconnect.web3.modal.data.network.model.GetWalletsDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface Web3ModalService {

    @GET("getWallets")
    suspend fun getWallets(
        @Query("page") page: Int,
        @Query("exclude") exclude: String,
        @Query("entries") entries: Int = 100,
        @Query("platform") platform: String = "android"
    ): Response<GetWalletsDTO>

}