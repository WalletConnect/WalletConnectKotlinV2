package com.walletconnect.web3.modal.data

import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.data.json_rpc.balance.BalanceRequest
import com.walletconnect.web3.modal.data.json_rpc.balance.BalanceRpcResponse
import com.walletconnect.web3.modal.data.network.BalanceService
import com.walletconnect.web3.modal.domain.model.Balance

internal class BalanceRpcRepository(
    private val balanceService: BalanceService,
    private val logger: Logger,
) {

    suspend fun getBalance(
        token: Modal.Model.Token, rpcUrl: String, address: String
    ) = runCatching {
        balanceService.getBalance(
            url = rpcUrl, body = BalanceRequest(address = address)
        )
    }.mapCatching { response ->
        response.body()!!.mapResponse(token)
    }.onFailure {
        logger.error(it)
    }.getOrNull()
}

private fun BalanceRpcResponse.mapResponse(token: Modal.Model.Token) = when {
    result != null -> Balance(token, result)
    error != null -> throw Throwable(error.message)
    else -> throw Throwable("Invalid balance response")
}
