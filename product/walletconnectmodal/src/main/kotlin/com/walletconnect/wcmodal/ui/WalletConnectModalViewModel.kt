package com.walletconnect.wcmodal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.modal.ui.model.LoadingState
import com.walletconnect.modal.ui.model.UiState
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.domain.dataStore.WalletDataSource
import com.walletconnect.wcmodal.domain.dataStore.WalletsData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class WalletConnectModalViewModel : ViewModel() {

    private val logger: Logger = wcKoinApp.koin.get()
    private val walletsDataStore = WalletDataSource { handleError(it) }

    val walletsState: StateFlow<WalletsData> = walletsDataStore.searchWalletsState.stateIn(viewModelScope, SharingStarted.Lazily, WalletsData.empty())

    val uiState: StateFlow<UiState<List<Wallet>>> = walletsDataStore.walletState.map { pagingData ->
        when {
            pagingData.error != null -> UiState.Error(pagingData.error)
            pagingData.loadingState == LoadingState.REFRESH -> UiState.Loading()
            else -> UiState.Success(pagingData.wallets)
        }
    }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val searchPhrase
        get() = walletsDataStore.searchPhrase


    init {
        fetchInitialWallets()
    }

    fun fetchInitialWallets() {
        viewModelScope.launch { walletsDataStore.fetchInitialWallets() }
    }

    fun connect(onSuccess: (String) -> Unit) {
        try {
            val pairing =
                CoreClient.Pairing.create { error ->
                    throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
                }!!

            val sessionParams = WalletConnectModal.sessionParams
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )

            WalletConnectModal.connect(
                connect = connectParams,
                onSuccess = { url -> viewModelScope.launch { onSuccess(url) } },
                onError = { handleError(it.throwable) }
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }

    fun fetchMoreWallets() {
        viewModelScope.launch { walletsDataStore.fetchMoreWallets() }
    }

    fun search(searchPhrase: String) {
        viewModelScope.launch { walletsDataStore.searchWallet(searchPhrase) }
    }

    fun saveRecentWallet(wallet: Wallet) {
        walletsDataStore.updateRecentWallet(wallet.id)
    }

    fun clearSearch() = walletsDataStore.clearSearch()

    fun getWallet(walletId: String?) = walletsDataStore.getWallet(walletId)

    fun getNotInstalledWallets() = walletsDataStore.wallets.filterNot { it.isWalletInstalled }

    private fun handleError(error: Throwable) {
        logger.error(error)
    }
}