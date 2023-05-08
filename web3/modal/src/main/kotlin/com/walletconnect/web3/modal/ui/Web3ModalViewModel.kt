package com.walletconnect.web3.modal.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.configuration.CONFIGURATION
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.domain.configuration.Web3ModalConfigSerializer
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.client.Web3Modal as Client
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

internal class Web3ModalViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val configuration = savedStateHandle.get<String>(CONFIGURATION)?.let { config ->
        Web3ModalConfigSerializer.deserialize(config)
    }

    private val _modalState: MutableStateFlow<Web3ModalState> =
        MutableStateFlow(Web3ModalState.Loading)

    val modalState: StateFlow<Web3ModalState>
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
                is Config.Connect -> connectWallet(configuration)
                else -> throw IllegalStateException("Invalid web3modal configuration")
            }
        }
    }

    private fun connectWallet(configuration: Config.Connect) {
        if (configuration.uri != null) {
            _modalState.value = Web3ModalState.ConnectState(configuration.uri)
        } else {
            val pairing = CoreClient.Pairing.create { error ->
                throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
            }!!

            val connectParams = Modal.Params.Connect(
                namespaces = configuration.namespaces,
                optionalNamespaces = configuration.optionalNamespaces,
                pairing = pairing
            )

            Client.connect(
                connect = connectParams,
                onSuccess = {
                    _modalState.value = Web3ModalState.ConnectState(pairing.uri)
                },
                onError = { error ->
                    Timber.e(error.throwable.stackTraceToString())
                })
        }
    }
}

