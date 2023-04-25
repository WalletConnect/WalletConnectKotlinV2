package com.walletconnect.sample.dapp.web3modal.domain.usecases

import com.walletconnect.sample.dapp.web3modal.data.explorer.ExplorerRepository
import com.walletconnect.sample.dapp.web3modal.domain.model.WalletRecommendation
import com.walletconnect.sample.dapp.web3modal.domain.model.toWalletRecommendation

interface GetWalletsRecommendationsUseCase {
    suspend operator fun invoke(
        chains: List<String>
    ): List<WalletRecommendation>
}

private const val RECOMMENDED_WALLET_AMOUNT = 9

class GetWalletsRecommendationsUseCaseImpl(
    private val explorerRepository: ExplorerRepository
) : GetWalletsRecommendationsUseCase {
    override suspend fun invoke(
        chains: List<String>
    ): List<WalletRecommendation> {
        return explorerRepository.getWalletsList(
            page = 1,
            entries = RECOMMENDED_WALLET_AMOUNT,
            chains = chains
        )?.wallets?.map { it.toWalletRecommendation() } ?: listOf()
    }

}