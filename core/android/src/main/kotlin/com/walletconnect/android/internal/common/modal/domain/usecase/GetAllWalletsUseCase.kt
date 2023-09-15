package com.walletconnect.android.internal.common.modal.domain.usecase

import com.walletconnect.android.internal.common.modal.Web3ModalApiRepository
import com.walletconnect.android.internal.common.modal.data.model.Wallet

class GetAllWalletsUseCase(
    private val web3ModalApiRepository: Web3ModalApiRepository
) {
    suspend operator fun invoke(
        sdkType: String,
        excludeIds: List<String> = listOf(),
        recommendedWalletsIds: List<String> = listOf(),
    ): List<Wallet> = web3ModalApiRepository.fetchAllWallets(
        sdkType = sdkType,
        excludeIds = excludeIds,
        recommendedWalletsIds = recommendedWalletsIds
    )
}