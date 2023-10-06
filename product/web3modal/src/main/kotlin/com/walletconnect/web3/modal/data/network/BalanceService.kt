package com.walletconnect.web3.modal.data.network

import com.walletconnect.web3.modal.data.json_rpc.balance.BalanceRequest
import com.walletconnect.web3.modal.data.json_rpc.balance.BalanceRpcResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

internal interface BalanceService {

    @POST
    suspend fun getBalance(
        @Url url: String,
        @Body body: BalanceRequest
    ): Response<BalanceRpcResponse>
}
