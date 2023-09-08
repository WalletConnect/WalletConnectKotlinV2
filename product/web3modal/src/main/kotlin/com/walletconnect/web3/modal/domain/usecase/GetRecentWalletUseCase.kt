package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.RecentWalletsRepository

internal class GetRecentWalletUseCase(private val repository: RecentWalletsRepository) {
    operator fun invoke() = repository.getRecentWalletId()
}