package com.walletconnect.android.internal.common.explorer.domain.usecase

import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.model.Wallet

interface GetWalletsUseCaseInterface {
    suspend operator fun invoke(
        sdkType: String,
        chains: String?
    ): List<Wallet>
}


class GetWalletsUseCase(
    private val explorerRepository: ExplorerRepository
) : GetWalletsUseCaseInterface {
    override suspend fun invoke(
        sdkType: String,
        chains: String?
    ): List<Wallet> {
        return explorerRepository.getMobileWallets(
            sdkType = sdkType,
            chains = chains
        ).listing
    }
}
