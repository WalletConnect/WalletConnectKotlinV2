package com.walletconnect.wcmodal.domain.usecase

import com.walletconnect.wcmodal.domain.RecentWalletsRepository

class SaveRecentWalletUseCase(
    private val repository: RecentWalletsRepository
) {
    operator fun invoke(id: String) = repository.saveRecentWalletId(id)
}