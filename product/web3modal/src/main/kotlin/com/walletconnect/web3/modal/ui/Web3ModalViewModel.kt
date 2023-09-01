package com.walletconnect.web3.modal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val W3M_SDK = "w3m"

internal class Web3ModalViewModel : ViewModel() {

    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()
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
        getActiveSession()?.let { activeSession ->
            createAccountModalState(activeSession)
        } ?: createConnectModalState()
    }

    private fun getActiveSession(): Modal.Model.Session? {
        val sessionTopic = getSessionTopicUseCase()
        return if (sessionTopic != null) {
            Web3Modal.getActiveSessionByTopic(sessionTopic)
        } else {
            null
        }
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
            Web3Modal.connect(connectParams, onSuccess) { Timber.e(it.throwable) }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    internal fun createAccountModalState(activeSession: Modal.Model.Session) {
        val selectedChain = activeSession.namespaces.getSelectedChain()
        val accountData = AccountData(
            topic = activeSession.topic,
            address = activeSession.namespaces.getAddress(selectedChain),
            balance = "",
            selectedChain = Chain(selectedChain),
            chains = activeSession.namespaces.keys.map { Chain(it) }
        )
        _modalState.value = Web3ModalState.AccountState(accountData)
    }

    private fun Map<String, Modal.Model.Namespace.Session>.getSelectedChain() = keys.find { it == getSelectedChainUseCase() } ?: keys.first()
    private fun Map<String, Modal.Model.Namespace.Session>.getAddress(selectedChain: String) = get(selectedChain)?.accounts?.first()?.split(":")?.last() ?: String.Empty

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
        viewModelScope.launch {
            try {
                val wallets = if (Web3Modal.recommendedWalletsIds.isEmpty()) {
                    getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds)
                } else {
                    getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds, recommendedIds = Web3Modal.recommendedWalletsIds).union(
                        getWalletsUseCase(sdkType = W3M_SDK, chains = chains, excludedIds = Web3Modal.excludedWalletsIds)
                    ).toList()
                }
                _modalState.value = Web3ModalState.Connect(uri, wallets)
            } catch (e: Exception) {
                Timber.e(e)
                _modalState.value = Web3ModalState.Connect(uri)
            }
        }
    }

    internal fun saveSessionTopic(topic: String) = saveSessionTopicUseCase(topic)

    internal fun disconnect(
        topic: String,
        onSuccess: () -> Unit
    ) {
        Web3Modal.disconnect(
            disconnect = Modal.Params.Disconnect(topic),
            onSuccess = {
                deleteSessionDataUseCase()
                onSuccess()
            },
            onError = {
                Timber.e(it.throwable)
            }
        )
    }

    internal fun changeChain(chain: Chain) {
        (_modalState.value as? Web3ModalState.AccountState)?.accountData?.let { accountData ->
            saveChainSelectionUseCase(chain.id)
            _modalState.value = Web3ModalState.AccountState(accountData.copy(selectedChain = chain))
        }
    }

    private fun handleError(error: Throwable) {
        _modalState.value = Web3ModalState.Error(error)
    }
}
