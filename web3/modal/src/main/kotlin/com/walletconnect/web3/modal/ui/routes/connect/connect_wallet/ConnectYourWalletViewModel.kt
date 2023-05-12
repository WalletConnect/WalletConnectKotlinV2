package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.domain.usecases.GetWalletsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

internal class ConnectYourWalletViewModel: ViewModel() {

    private val getWalletsRecommendationsUseCase: GetWalletsUseCase = wcKoinApp.koin.get()

    private val _uiState = MutableStateFlow(ConnectYourWalletUI())

    val uiState: StateFlow<ConnectYourWalletUI>
        get() = _uiState.asStateFlow()

    fun getWalletRecommendations() {
        viewModelScope.launch {
            try {
                val wallets = getWalletsRecommendationsUseCase(listOf())
                _uiState.value = ConnectYourWalletUI(wallets)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = ConnectYourWalletUI()
            }

        }
    }

}