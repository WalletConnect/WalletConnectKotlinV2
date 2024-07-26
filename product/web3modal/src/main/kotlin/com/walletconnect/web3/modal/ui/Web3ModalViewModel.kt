package com.walletconnect.web3.modal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class Web3ModalViewModel : ViewModel() {

    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()
    private val _modalState: MutableStateFlow<Web3ModalState> = MutableStateFlow(Web3ModalState.Loading)
    val shouldDisconnect get() = web3ModalEngine.shouldDisconnect

    val modalState: StateFlow<Web3ModalState>
        get() = _modalState.asStateFlow()

    init {
        require(Web3Modal.chains.isNotEmpty()) { "Be sure to set the Chains using Web3Modal.setChains" }
        initModalState()
    }

    fun disconnect() {
        web3ModalEngine.disconnect(
            onSuccess = { println("Disconnected successfully") },
            onError = { println("Disconnect error: $it") }
        )
    }

    internal fun initModalState() {
        viewModelScope.launch {
            web3ModalEngine.getActiveSession()?.let { _ ->
                createAccountModalState()
            } ?: createConnectModalState()
        }
    }

    private fun createAccountModalState() {
        _modalState.value = Web3ModalState.AccountState
    }

    private fun createConnectModalState() {
        _modalState.value = Web3ModalState.Connect
    }
}
