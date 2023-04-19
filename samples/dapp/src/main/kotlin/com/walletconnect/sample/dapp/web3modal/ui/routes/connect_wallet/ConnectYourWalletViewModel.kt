package com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.sample.dapp.web3modal.domain.usecases.GetWalletsRecommendationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ConnectYourWalletViewModel(
    private val getWalletsRecommendationsUseCase: GetWalletsRecommendationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConnectYourWalletUI>(ConnectYourWalletUI.Loading)

    val uiState: StateFlow<ConnectYourWalletUI>
        get() = _uiState.asStateFlow()

    fun getWalletRecommendations() {
        viewModelScope.launch {
            try {
                val wallets = getWalletsRecommendationsUseCase(listOf())
                _uiState.value = ConnectYourWalletUI.SelectWallet(wallets)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = ConnectYourWalletUI.Empty
            }

        }
    }

}