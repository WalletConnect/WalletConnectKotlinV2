package com.walletconnect.android.internal.common.explorer.domain.usecase

import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Wallet

interface GetWalletsUseCaseInterface {
    suspend operator fun invoke(
        modalVersion: String,
        chains: String?
    ): List<Wallet>
}


class GetWalletsUseCase(
    private val explorerRepository: ExplorerRepository
) : GetWalletsUseCaseInterface {
    override suspend fun invoke(
        modalVersion: String,
        chains: String?
    ): List<Wallet> {
        return explorerRepository.getMobileWallets(
            modalVersion = modalVersion,
            chains = chains
        ).listing
    }
}
