package com.walletconnect.web3.modal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class Web3ModalViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shouldOpenChooseNetwork = savedStateHandle.get<Boolean>(CHOOSE_NETWORK_KEY) ?: false

    private val logger: Logger = wcKoinApp.koin.get()

    private val saveSessionTopicUseCase: SaveSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()

    private val _modalState: MutableStateFlow<Web3ModalState> = MutableStateFlow(Web3ModalState.Loading)

    val modalState: StateFlow<Web3ModalState>
        get() = _modalState.asStateFlow()

    init {
        require(Web3Modal.chains.isNotEmpty()) { "Be sure to set the Chains using Web3Modal.setChains" }
        initModalState()
    }

    internal fun initModalState() {
        viewModelScope.launch {
            getActiveSession()?.let { activeSession ->
                createAccountModalState(activeSession)
            } ?: createConnectModalState()
        }
    }

    private suspend fun getActiveSession(): Modal.Model.Session? =
        getSessionTopicUseCase()?.let {
            Web3Modal.getActiveSessionByTopic(it)
        }

    internal suspend fun createAccountModalState(activeSession: Modal.Model.Session) {
        val chains = activeSession.getChains()
        val selectedChain = chains.getSelectedChain(getSelectedChainUseCase())
        val address = activeSession.getAddress(selectedChain)

        val accountData = AccountData(
            topic = activeSession.topic,
            address = address,
            balance = "",
            selectedChain = selectedChain,
            chains = chains
        )
        _modalState.value = Web3ModalState.AccountState(accountData)
    }

    private fun createConnectModalState() {
        _modalState.value = Web3ModalState.Connect(shouldOpenChooseNetwork)
    }
    internal fun saveSessionTopic(topic: String) = viewModelScope.launch {
        saveSessionTopicUseCase(topic)
    }

    internal fun disconnect(
        topic: String,
        onSuccess: () -> Unit
    ) {
        Web3Modal.disconnect(
            disconnect = Modal.Params.Disconnect(topic),
            onSuccess = {
                viewModelScope.launch {
                    deleteSessionDataUseCase()
                }
                onSuccess()
            },
            onError = {
                logger.error(it.throwable)
            }
        )
    }

    internal fun changeChain(accountData: AccountData, chain: Chain) {
        viewModelScope.launch {
            saveChainSelectionUseCase(chain.id)
            val address = Web3Modal.getActiveSessionByTopic(accountData.topic)?.getAddress(chain) ?: accountData.address
            _modalState.value = Web3ModalState.AccountState(
                accountData.copy(
                    selectedChain = chain,
                    address = address
                )
            )
        }
    }
}
