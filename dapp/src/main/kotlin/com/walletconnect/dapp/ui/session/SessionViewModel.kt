package com.walletconnect.dapp.ui.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val _sessionUI: MutableStateFlow<List<SessionUI>> = MutableStateFlow(getListOfAccounts())
    val uiState: StateFlow<List<SessionUI>> = _sessionUI.asStateFlow()

    private val _navigationEvents: MutableSharedFlow<SampleDappEvents> = MutableSharedFlow()
    val navigationEvents: SharedFlow<SampleDappEvents> = _navigationEvents.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { walletEvent ->
                when (walletEvent) {
                    is WalletConnect.Model.UpdatedSessionAccounts -> {
                        val listOfAccounts = getListOfAccounts(walletEvent.topic)
                        _sessionUI.value = listOfAccounts
                    }
                    is WalletConnect.Model.DeletedSession -> {
                        _navigationEvents.emit(SampleDappEvents.Disconnect)
                    }
                    else -> Unit
                }
            }.launchIn(viewModelScope)

    }

    private fun getListOfAccounts(topic: String? = null): List<SessionUI> {
        return WalletConnectClient.getListOfSettledSessions().filter {
            if (topic != null) {
                it.topic == topic
            } else {
                it.topic == DappDelegate.selectedSessionTopic
            }
        }.flatMap { settledSession ->
            settledSession.accounts
        }.map {
            val (parentChain, chainId, account) = it.split(":")

            val chain = EthTestChains.values().first { chain ->
                chain.parentChain == parentChain && chain.chainId == chainId.toInt()
            }

            SessionUI(chain.icon, chain.name, account, chain.parentChain, chain.chainId)
        }
    }

    fun ping() {
        val pingParams = WalletConnect.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        WalletConnectClient.ping(pingParams, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: WalletConnect.Model.Ping.Success) {
                _navigationEvents.tryEmit(SampleDappEvents.PingSuccess(pingSuccess.topic))
            }

            override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                _navigationEvents.tryEmit(SampleDappEvents.PingError)
            }
        })
    }

    fun disconnect() {
        if (DappDelegate.selectedSessionTopic != null) {
            val disconnectParams = WalletConnect.Params.Disconnect(
                sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic),
                reason = "Disconnect Clicked",
                reasonCode = 400
            )

            WalletConnectClient.disconnect(disconnectParams) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
            DappDelegate.deselectAccountDetails()
        }

        viewModelScope.launch {
            _navigationEvents.emit(SampleDappEvents.Disconnect)
        }
    }
}