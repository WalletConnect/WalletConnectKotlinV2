package com.walletconnect.web3.modal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
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

private const val W3M_SDK = "w3m"

internal class Web3ModalViewModel : ViewModel() {

    private val logger: Logger = wcKoinApp.koin.get()

    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()

    private val getRecentWalletUseCase: GetRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveSessionTopicUseCase: SaveSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase = wcKoinApp.koin.get()
    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()

    private val pairing by lazy {
        CoreClient.Pairing.create { error ->
            throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
        }!!
    }

    private val _modalState: MutableStateFlow<Web3ModalState> = MutableStateFlow(Web3ModalState.Loading)

    val modalState: StateFlow<Web3ModalState>
        get() = _modalState.asStateFlow()

    init {
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

    internal fun retryConnection(onSuccess: () -> Unit) {
        try {
            val sessionParams = Web3Modal.sessionParams
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )
            Web3Modal.connect(connectParams, onSuccess) { logger.error(it.throwable) }
        } catch (e: Exception) {
            handleError(e)
        }
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

    internal fun createConnectModalState() {
        val sessionParams = Web3Modal.sessionParams
        try {
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )
            val chains = sessionParams.requiredNamespaces.values.toList().mapNotNull { it.chains?.joinToString() }.joinToString()
            Web3Modal.connect(
                connect = connectParams,
                onSuccess = { viewModelScope.launch { fetchWallets(pairing.uri, chains) } },
                onError = { handleError(it.throwable) }
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private suspend fun fetchWallets(uri: String, chains: String) {
        try {
            val wallets = if (Web3Modal.recommendedWalletsIds.isEmpty()) {
                getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds)
            } else {
                getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds, recommendedIds = Web3Modal.recommendedWalletsIds).union(
                    getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds)
                ).toList()
            }
            _modalState.value = Web3ModalState.Connect(uri, wallets.mapRecentWallet(getRecentWalletUseCase()))
        } catch (e: Exception) {
            logger.error(e)
            handleError(e)
        }
    }

    fun updateRecentWalletId(id: String) =
        (_modalState.value as? Web3ModalState.Connect)?.let {
            saveRecentWalletUseCase(id)
            _modalState.value = it.copy(wallets = it.wallets.mapRecentWallet(id))
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

    private fun handleError(error: Throwable) {
        _modalState.value = Web3ModalState.Error(error)
    }
}

private fun List<Wallet>.mapRecentWallet(id: String?) = map {
    it.apply { it.isRecent = it.id == id }
}.sortedWith(compareByDescending<Wallet> { it.isRecent }.thenByDescending { it.isWalletInstalled })
