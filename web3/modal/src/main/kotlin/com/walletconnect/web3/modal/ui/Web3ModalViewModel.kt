package com.walletconnect.web3.modal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.BuildConfig
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.configuration.CONFIG_ARG
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.domain.configuration.Web3ModalConfigSerializer
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

internal class Web3ModalViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val getWalletsRecommendationsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()

    private val configuration = savedStateHandle.get<String>(CONFIG_ARG)?.let { config ->
        Web3ModalConfigSerializer.deserialize(config)
    }

    private val _modalState: MutableStateFlow<Web3ModalState?> = MutableStateFlow(null)

    val modalState: StateFlow<Web3ModalState?>
        get() = _modalState.asStateFlow()

    private val _web3ModalEvents: MutableSharedFlow<Web3ModalEvents> = MutableSharedFlow()
    val modalEvents: SharedFlow<Web3ModalEvents>
        get() = _web3ModalEvents.asSharedFlow()

    private fun subscribeToWalletEvents() {
        Web3ModalDelegate.wcEventModels.map { event ->
            when (event) {
                is Modal.Model.ApprovedSession -> Web3ModalEvents.SessionApproved
                is Modal.Model.RejectedSession -> Web3ModalEvents.SessionRejected
                else -> Web3ModalEvents.NoAction
            }
        }.onEach { event ->
            _web3ModalEvents.emit(event)
        }
    }

    init {
        subscribeToWalletEvents()
        viewModelScope.launch {
            when (configuration) {
                is Config.Connect -> configuration.connectionState()
                else -> throw IllegalStateException("Invalid web3modal configuration")
            }
        }
    }

    private suspend fun Config.Connect.connectionState() {
        try {
            val wallets = getWalletsRecommendationsUseCase(BuildConfig.MODAL_VERSION, chains?.joinToString())
            _modalState.value = Web3ModalState.ConnectState(uri, wallets)
        } catch (e: Exception) {
            Timber.e(e)
            _modalState.value = Web3ModalState.ConnectState(uri)
        }
    }
}

