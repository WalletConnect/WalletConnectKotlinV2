package com.walletconnect.android.internal.common.explorer.domain.usecase

import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Wallet

interface GetWalletsUseCase {
    suspend operator fun invoke(
        chains: String?
    ): List<Wallet>
}


class GetWalletsUseCaseImpl(
    private val explorerRepository: ExplorerRepository
) : GetWalletsUseCase {
    override suspend fun invoke(
        chains: String?
    ): List<Wallet> {
        return explorerRepository.getMobileWallets(
            chains = chains
        ).listing
    }
}
