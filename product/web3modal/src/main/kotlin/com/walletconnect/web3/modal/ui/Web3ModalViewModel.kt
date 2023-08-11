package com.walletconnect.web3.modal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val W3M_SDK = "w3m"

internal class Web3ModalViewModel : ViewModel() {

    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()

    private val pairing by lazy {
        CoreClient.Pairing.create { error ->
            throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
        }!!
    }

    private val _modalState: MutableStateFlow<Web3ModalState> = MutableStateFlow(Web3ModalState.Loading)

    val modalState: StateFlow<Web3ModalState>
        get() = _modalState.asStateFlow()

    private val _web3ModalEvents: MutableSharedFlow<Web3ModalEvents> = MutableSharedFlow()
    val modalEvents: SharedFlow<Web3ModalEvents>
        get() = _web3ModalEvents.asSharedFlow()

    init {
        initModalState()
    }

    internal fun initModalState() {
        //TODO ADD CHECK IF THERE IS ANY ACCOUNT LOGGED RIGHT NOW, For now leave just connect state
        createConnectModalState()
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
                onSuccess = { viewModelScope.launch { fetchWallets(pairing.uri, chains) }},
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

    private fun handleError(error: Throwable) {
        _modalState.value = Web3ModalState.Error(error)
    }
}

