package com.walletconnect.dapp.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.Chains
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class SessionViewModel : ViewModel() {
    private val _dappEvents: MutableStateFlow<Pair<Long, WalletConnect.Model?>> = MutableStateFlow(Pair(0, null))
    val navigation: LiveData<NavigationEvents> =
        DappDelegate.wcEventModels.combine(_dappEvents) { (walletEventTimestamp, walletEvent: WalletConnect.Model?), (dappEventTimestamp, dappEvent: WalletConnect.Model?) ->
            if (dappEventTimestamp > walletEventTimestamp) {
                when (dappEvent) {
                    is WalletConnect.Model.Ping.Success -> NavigationEvents.PingSuccess(dappEvent.topic)
                    is WalletConnect.Model.Ping.Error -> NavigationEvents.PingError
                    is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
                    else -> NavigationEvents.NoAction
                }
            } else {
                when (walletEvent) {
                    is WalletConnect.Model.UpdatedSession -> {
                        val listOfAccounts = getListOfAccounts(walletEvent.topic)
                        NavigationEvents.UpdatedListOfAccounts(listOfAccounts)
                    }
                    is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
                    else -> NavigationEvents.NoAction
                }
            }
        }.asLiveData(viewModelScope.coroutineContext)

    fun getListOfAccounts(topic: String? = null): List<SessionUI> {
        return WalletConnectClient.getListOfSettledSessions().filter {
            if (topic != null) {
                it.topic == topic
            } else {
                it.topic == DappDelegate.selectedSessionTopic
            }
        }.map { settledSession ->
            settledSession.accounts
        }.flatten().map {
            val (parentChain, chainId, account) = it.split(":")

            val chain = Chains.values().first { chain ->
                chain.parentChain == parentChain && chain.chainId == chainId.toInt()
            }

            SessionUI(chain.icon, chain.name, account, chain.parentChain, chain.chainId)
        }
    }

    fun ping() {
        val pingParams = WalletConnect.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        WalletConnectClient.ping(pingParams, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: WalletConnect.Model.Ping.Success) {
                _dappEvents.tryEmit(System.currentTimeMillis() to pingSuccess)
            }

            override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                _dappEvents.tryEmit(System.currentTimeMillis() to pingError)
            }
        })
    }

    fun disconnect() {
        val disconnectParams = WalletConnect.Params.Disconnect(
            sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic),
            reason = "Disconnect Clicked",
            reasonCode = 400
        )

        WalletConnectClient.disconnect(disconnectParams)
    }
}