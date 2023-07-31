package com.walletconnect.wcmodal.domain.usecase

import com.walletconnect.wcmodal.domain.RecentWalletsRepository

class GetRecentWalletUseCase(private val repository: RecentWalletsRepository) {
    operator fun invoke() = repository.getRecentWalletId()
}