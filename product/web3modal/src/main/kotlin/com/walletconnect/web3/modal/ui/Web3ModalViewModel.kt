package com.walletconnect.web3.modal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.Session
import com.walletconnect.web3.modal.domain.usecase.SaveSessionUseCase
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import com.walletconnect.web3.modal.utils.getAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class Web3ModalViewModel : ViewModel() {

    private val saveSessionUseCase: SaveSessionUseCase = wcKoinApp.koin.get()
    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()

    private val _modalState: MutableStateFlow<Web3ModalState> = MutableStateFlow(Web3ModalState.Loading)

    val modalState: StateFlow<Web3ModalState>
        get() = _modalState.asStateFlow()

    init {
        require(Web3Modal.chains.isNotEmpty()) { "Be sure to set the Chains using Web3Modal.setChains" }
        initModalState()
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

    internal fun saveSession(event: Modal.Model.ApprovedSession) = viewModelScope.launch {
        val chain = Web3Modal.selectedChain ?: web3ModalEngine.getSelectedChainOrFirst()
        val session = event.toSession(chain)
        saveSessionUseCase(session)
    }
}

private fun Modal.Model.ApprovedSession.toSession(chain: Modal.Model.Chain) = when (val approvedSession = this) {
    is Modal.Model.ApprovedSession.WalletConnectSession -> Session.WalletConnect(chain = chain.id, topic = approvedSession.topic, address = approvedSession.getAddress(chain))
    is Modal.Model.ApprovedSession.CoinbaseSession -> Session.Coinbase(chain = "eip155:${approvedSession.networkId}", approvedSession.address)
}
