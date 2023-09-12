package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.Web3ModalApiRepository
import com.walletconnect.web3.modal.domain.model.Wallet

internal class GetAllWalletsUseCase(
    private val web3ModalApiRepository: Web3ModalApiRepository
) {
    suspend operator fun invoke(
        excludeIds: List<String> = listOf(),
        recommendedWalletsIds: List<String> = listOf()
    ): List<Wallet> = web3ModalApiRepository.fetchAllWallets(
        excludeIds = excludeIds,
        recommendedWalletsIds = recommendedWalletsIds
    )
}