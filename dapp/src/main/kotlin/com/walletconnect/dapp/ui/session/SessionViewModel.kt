package com.walletconnect.dapp.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.Chains
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class SessionViewModel : ViewModel() {
    private val navigationChannel = Channel<NavigationEvents>(Channel.BUFFERED)
    val navigation = navigationChannel.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent ->
            when (walletEvent) {
                is WalletConnect.Model.UpdatedSession -> {
                    val listOfAccounts = getListOfAccounts(walletEvent.topic)
                    NavigationEvents.UpdatedListOfAccounts(listOfAccounts)
                }
                is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
                else -> NavigationEvents.NoAction
            }
        }.onEach { navigationEvents ->
            navigationChannel.trySend(navigationEvents)
        }.launchIn(viewModelScope)
    }

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
                navigationChannel.trySend(NavigationEvents.PingSuccess(pingSuccess.topic))
            }

            override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                navigationChannel.trySend(NavigationEvents.PingError)
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

            WalletConnectClient.disconnect(disconnectParams)
            DappDelegate.deselectAccountDetails()
        }

        navigationChannel.trySend(NavigationEvents.Disconnect)
    }
}