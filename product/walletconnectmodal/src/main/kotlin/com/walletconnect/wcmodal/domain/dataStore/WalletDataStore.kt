package com.walletconnect.wcmodal.domain.dataStore

import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.modal.domain.usecase.GetInstalledWalletsIdsUseCaseInterface
import com.walletconnect.android.internal.common.modal.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.modal.ui.model.LoadingState
import com.walletconnect.util.Empty
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.domain.usecase.GetRecentWalletUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private const val WCM_SDK = "wcm"

private data class ListingData(
    var page: Int = 1,
    var totalCount: Int = 0,
    var wallets: List<Wallet> = listOf()
) {
    fun addNextPage(wallets: List<Wallet>) {
        page += 1
        this.wallets = this.wallets + wallets
    }
}

internal class WalletDataSource(
    private val showError: (Exception) -> Unit
) {
    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()
    private val getWalletsAppDataUseCase: GetInstalledWalletsIdsUseCaseInterface = wcKoinApp.koin.get()
    private val getRecentWalletUseCase: GetRecentWalletUseCase = wcKoinApp.koin.get()

    private var installedWalletsIds: List<String> = listOf()

    private var walletsListingData = ListingData()
    private var searchListingData = ListingData()

    var searchPhrase = String.Empty

    val totalWalletsCount: Int
        get() = walletsListingData.totalCount + installedWalletsIds.size

    val wallets: List<Wallet>
        get() = walletsListingData.wallets

    private fun getPriorityWallets() = (getRecentWalletUseCase()?.let { listOf(it) } ?: listOf()) + installedWalletsIds + WalletConnectModal.recommendedWalletsIds
    private val searchState: MutableStateFlow<WalletsData> = MutableStateFlow(WalletsData.empty())
    val walletState: MutableStateFlow<WalletsData> = MutableStateFlow(WalletsData.empty())

    val searchWalletsState = combine(walletState, searchState) { state, search ->
        if (searchPhrase.isEmpty()) { state } else { search }
    }

    suspend fun fetchInitialWallets() {
        walletState.value = WalletsData.refresh()
        try {
            fetchWalletsAppData()
            val installedWallets = fetchInstalledAndRecommendedWallets()
            val walletsListing = getWalletsUseCase(sdkType = WCM_SDK, page = 1, excludeIds = getPriorityWallets() + WalletConnectModal.excludedWalletsIds)
            walletsListingData = ListingData(page = 1, totalCount = walletsListing.totalCount, wallets = (installedWallets.wallets + walletsListing.wallets).mapRecentWallet(getRecentWalletUseCase()))
            walletState.value = WalletsData.submit(walletsListingData.wallets)
        } catch (exception: Exception) {
            showError(exception)
            walletState.value = WalletsData.error(error = exception)
        }
    }

    fun updateRecentWallet(walletId: String) {
        walletState.tryEmit(WalletsData.append(walletState.value.wallets.mapRecentWallet(walletId)))
    }

    private suspend fun fetchWalletsAppData() {
        installedWalletsIds = getWalletsAppDataUseCase(sdkType = WCM_SDK)
    }

    private suspend fun fetchInstalledAndRecommendedWallets() = getWalletsUseCase(
        sdkType = WCM_SDK,
        page = 1,
        includes = getPriorityWallets(),
        excludeIds = WalletConnectModal.excludedWalletsIds
    )

    suspend fun fetchMoreWallets() {
        if (searchPhrase.isEmpty()) {
            if (walletsListingData.wallets.size < walletsListingData.totalCount) {
                try {
                    walletState.value = WalletsData.append(walletsListingData.wallets)
                    val response = getWalletsUseCase(sdkType = WCM_SDK, page = walletsListingData.page + 1, excludeIds = getPriorityWallets() + WalletConnectModal.excludedWalletsIds)
                    walletsListingData.addNextPage(response.wallets)
                    walletState.value = WalletsData.submit(walletsListingData.wallets)
                } catch (exception: Exception) {
                    showError(exception)
                    walletState.value = WalletsData.error(walletsListingData.wallets, error = exception)
                }
            }
        } else {
            fetchNextSearchPage()
        }
    }

    fun clearSearch() {
        searchState.value = WalletsData.empty()
        searchPhrase = String.Empty
    }

    suspend fun searchWallet(searchPhrase: String) {
        if (searchPhrase.isEmpty()) {
            clearSearch()
        } else {
            this.searchPhrase = searchPhrase
            if (walletsListingData.wallets.size < walletsListingData.totalCount) {
                try {
                    searchState.value = WalletsData.refresh()
                    val searchResponse = getWalletsUseCase(sdkType = WCM_SDK, search = searchPhrase, page = 1, excludeIds = WalletConnectModal.excludedWalletsIds)
                    searchListingData = ListingData(page = 1, totalCount = searchResponse.totalCount, wallets = searchResponse.wallets)
                    searchState.value = WalletsData.submit(wallets = searchListingData.wallets)
                } catch (exception: Exception) {
                    showError(exception)
                    searchState.value = WalletsData.error(walletsListingData.wallets, error = exception)
                }
            } else {
                val searchedWallets = walletsListingData.wallets.filteredWallets(searchPhrase)
                searchState.value = WalletsData.submit(wallets = searchedWallets)
            }
        }
    }

    private suspend fun fetchNextSearchPage() {
        if (searchListingData.wallets.size < searchListingData.totalCount) {
            try {
                searchState.value = WalletsData.append(searchListingData.wallets)
                val searchResponse = getWalletsUseCase(sdkType = WCM_SDK, search = searchPhrase, page = searchListingData.page + 1, excludeIds = WalletConnectModal.excludedWalletsIds)
                searchListingData.addNextPage(searchResponse.wallets)
                searchState.value = WalletsData.submit(searchListingData.wallets)
            } catch (exception: Exception) {
                showError(exception)
                searchState.value = WalletsData.error(searchListingData.wallets, error = exception)
            }
        }
    }

    fun getWallet(walletId: String?) = walletsListingData.wallets.find { wallet -> wallet.id == walletId }
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
