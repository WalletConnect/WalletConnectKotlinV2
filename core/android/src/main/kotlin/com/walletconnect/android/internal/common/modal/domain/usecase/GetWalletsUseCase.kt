package com.walletconnect.android.internal.common.modal.domain.usecase

import com.walletconnect.android.internal.common.modal.Web3ModalApiRepository
import com.walletconnect.android.internal.common.modal.data.model.WalletListing

interface GetWalletsUseCaseInterface {
    suspend operator fun invoke(
        sdkType: String,
        page: Int,
        search: String? = null,
        excludeIds: List<String>? = null,
        includes: List<String>? = null
    ): WalletListing
}

internal class GetWalletsUseCase(
    private val web3ModalApiRepository: Web3ModalApiRepository
) : GetWalletsUseCaseInterface {
    override suspend fun invoke(
        sdkType: String,
        page: Int,
        search: String?,
        excludeIds: List<String>?,
        includes: List<String>?
    ): WalletListing = web3ModalApiRepository.getWallets(sdkType, page, search, excludeIds, includes).getOrThrow()
}
