package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.RecentWalletsRepository

internal class SaveRecentWalletUseCase(
    private val repository: RecentWalletsRepository
) {
    operator fun invoke(id: String) = repository.saveRecentWalletId(id)
}