package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.data.BalanceRpcRepository

internal class GetEthBalanceUseCase(
    private val balanceRpcRepository: BalanceRpcRepository,
) {
    suspend operator fun invoke(
        token: Modal.Model.Token,
        rpcUrl: String,
        address: String
    ) = balanceRpcRepository.getBalance(
        token = token,
        rpcUrl = rpcUrl,
        address = address
    )
}
