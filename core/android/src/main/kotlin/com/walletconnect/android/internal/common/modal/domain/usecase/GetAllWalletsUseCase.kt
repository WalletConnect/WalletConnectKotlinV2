package com.walletconnect.android.internal.common.modal.domain.usecase

import com.walletconnect.android.internal.common.modal.Web3ModalApiRepository
import com.walletconnect.android.internal.common.modal.data.model.Wallet

interface GetAllWalletsUseCaseInterface {
    suspend operator fun invoke(
        sdkType: String,
        excludeIds: List<String> = listOf(),
        recommendedWalletsIds: List<String> = listOf(),
    ): List<Wallet>
}

internal class GetAllWalletsUseCase(
    private val web3ModalApiRepository: Web3ModalApiRepository
): GetAllWalletsUseCaseInterface {
    override suspend fun invoke(
        sdkType: String,
        excludeIds: List<String>,
        recommendedWalletsIds: List<String>,
    ): List<Wallet> = web3ModalApiRepository.fetchAllWallets(
        sdkType = sdkType,
        excludeIds = excludeIds,
        recommendedWalletsIds = recommendedWalletsIds
    )
}