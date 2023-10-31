package com.walletconnect.web3.modal.ui.routes.connect

import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.modal.domain.usecase.GetInstalledWalletsIdsUseCaseInterface
import com.walletconnect.android.internal.common.modal.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.ui.model.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow

internal class WalletDataSource(
    private val showError: (String?) -> Unit
) {
    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()
    private val getWalletsAppDataUseCase: GetInstalledWalletsIdsUseCaseInterface = wcKoinApp.koin.get()
    private val getRecentWalletUseCase: GetRecentWalletUseCase = wcKoinApp.koin.get()

    private var installedWalletsIds: List<String> = listOf()
    private var totalCount = 0
    private var page = 1
    private var wallets: List<Wallet> = listOf()
    private fun getPriorityWallets() =
        (getRecentWalletUseCase()?.let { listOf(it) } ?: listOf()) + installedWalletsIds + Web3Modal.recommendedWalletsIds

    val state: MutableStateFlow<WalletsData> = MutableStateFlow(WalletsData.empty())

    suspend fun fetchInitialWallets() {
        state.value = WalletsData.refresh()
        try {
            page = 1
            fetchWalletsAppData()
            val installedWallets = fetchInstalledAndRecommendedWallets()
            val walletsListing = getWalletsUseCase(sdkType = W3M_SDK, page = page++, excludeIds = getPriorityWallets() + Web3Modal.excludedWalletsIds)

            totalCount = walletsListing.totalCount
            wallets = (installedWallets.wallets + walletsListing.wallets).mapRecentWallet(getRecentWalletUseCase())
            state.value = WalletsData.submit(wallets)
        } catch (exception: Exception) {
            showError(exception.message)
            state.value = WalletsData.error(error = exception)
        }
    }

    fun updateRecentWallet(walletId: String) {
        state.tryEmit(WalletsData.append(state.value.wallets.mapRecentWallet(walletId)))
    }

    private suspend fun fetchWalletsAppData() = getWalletsAppDataUseCase(sdkType = W3M_SDK)

    private suspend fun fetchInstalledAndRecommendedWallets() = getWalletsUseCase(
        sdkType = W3M_SDK,
        page = 1,
        includes = getPriorityWallets(),
        excludeIds = Web3Modal.excludedWalletsIds
    )

    suspend fun fetchMoreWallets() {
        if (wallets.size < totalCount) {
            try {
                state.value = WalletsData.append(wallets)
                val response = getWalletsUseCase(sdkType = W3M_SDK, page = page++, excludeIds = getPriorityWallets() + Web3Modal.excludedWalletsIds)
                totalCount = response.totalCount
                wallets = wallets + response.wallets
                state.value = WalletsData.submit(wallets)
            } catch (exception: Exception) {
                showError(exception.message)
                state.value = WalletsData.error(wallets, error = exception)
            }
        }
    }

    fun clearSearch() {
        state.value = WalletsData.submit(wallets = wallets)
    }

    suspend fun searchWallet(searchPhrase: String) {
        if (searchPhrase.isEmpty()) {
            clearSearch()
        } else {
            if (wallets.size < totalCount) {
                try {
                    state.value = WalletsData.refresh()
                    val searchResponse = getWalletsUseCase(sdkType = W3M_SDK, search = searchPhrase, page = 1, excludeIds = Web3Modal.excludedWalletsIds)
                    state.value = WalletsData.submit(wallets = searchResponse.wallets)
                } catch (exception: Exception) {
                    showError(exception.message)
                    state.value = WalletsData.error(wallets, error = exception)
                }
            } else {
                val searchedWallets = wallets.filteredWallets(searchPhrase)
                state.value = WalletsData.submit(wallets = searchedWallets)
            }
        }
    }
}

private fun List<Wallet>.filteredWallets(value: String): List<Wallet> = this.filter { it.name.startsWith(prefix = value, ignoreCase = true) }

private fun List<Wallet>.mapRecentWallet(id: String?) = map {
    it.apply { it.isRecent = it.id == id }
}.sortedWith(compareByDescending<Wallet> { it.isRecent }.thenByDescending { it.isWalletInstalled })


internal data class WalletsData(
    val wallets: List<Wallet> = listOf(),
    val loadingState: LoadingState? = null,
    val error: Exception? = null
) {
    companion object {
        fun empty() = WalletsData()

        fun refresh() = WalletsData(loadingState = LoadingState.REFRESH)

        fun submit(wallets: List<Wallet>) = WalletsData(wallets)

        fun append(wallets: List<Wallet>) = WalletsData(wallets, LoadingState.APPEND)

        fun error(wallets: List<Wallet> = listOf(), error: Exception) = WalletsData(wallets = wallets, error = error)
    }
}
