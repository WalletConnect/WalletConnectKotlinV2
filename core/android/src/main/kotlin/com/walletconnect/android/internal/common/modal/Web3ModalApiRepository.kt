package com.walletconnect.android.internal.common.modal

import android.content.Context
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.modal.data.network.Web3ModalService
import com.walletconnect.android.internal.common.modal.data.network.model.WalletDTO
import com.walletconnect.android.utils.isWalletInstalled

class Web3ModalApiRepository(
    private val context: Context,
    private val web3ModalApiUrl: String,
    private val web3ModalService: Web3ModalService
) {
    suspend fun fetchAllWallets(
        sdkType: String,
        excludeIds: List<String> = listOf(),
        recommendedWalletsIds: List<String> = listOf()
    ): List<Wallet> {
        val exclude = excludeIds.joinToString(",")
        val wallets = mutableListOf<Wallet>()
        var page = 1
        var count: Int
        do {
            val response = web3ModalService.getWallets(sdkType = sdkType, page = page, exclude = exclude)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.let {
                    count = it.count
                    page++
                    wallets.addAll(it.data.toWallets(recommendedWalletsIds))
                }
            } else {
                throw Exception(response.errorBody()?.string())
            }
        } while (wallets.size != count)
        return wallets
    }

    private fun List<WalletDTO>.toWallets(
        recommendedWallets: List<String>
    ): List<Wallet> = map { walletDTO ->
        Wallet(
            id = walletDTO.id,
            name = walletDTO.name,
            homePage = walletDTO.homePage,
            imageUrl = web3ModalApiUrl + "getWalletImage/${walletDTO.imageId}",
            order = walletDTO.order,
            mobileLink = walletDTO.mobileLink,
            playStore = walletDTO.playStore,
            isRecommended = recommendedWallets.any { walletDTO.id == it }
        ).apply {
            isWalletInstalled = context.packageManager.isWalletInstalled(appPackage, mobileLink)
        }
    }
}
