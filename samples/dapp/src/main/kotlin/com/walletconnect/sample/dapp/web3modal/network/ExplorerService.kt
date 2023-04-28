package com.walletconnect.sample.dapp.web3modal.network

import com.walletconnect.sample.dapp.web3modal.network.model.ExplorerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExplorerService {

    @GET("/v3/wallets")
    suspend fun getWallets(
        @Query("projectId") projectId: String,
        @Query("page") page: Int,
        @Query("entries") entries: Int,
        @Query("chains") chains: String
    ): Response<ExplorerResponse>

}