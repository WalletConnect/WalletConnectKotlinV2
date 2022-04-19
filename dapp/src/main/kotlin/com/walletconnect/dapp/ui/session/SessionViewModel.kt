package com.walletconnect.dapp.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class SessionViewModel : ViewModel() {
    private val navigationChannel = Channel<SampleDappEvents>(Channel.BUFFERED)
    val navigation = navigationChannel.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent ->
            when (walletEvent) {
                is WalletConnect.Model.UpdatedSessionAccounts -> {
                    val listOfAccounts = getListOfAccounts(walletEvent.topic)
                    SampleDappEvents.UpdatedListOfAccounts(listOfAccounts)
                }
                is WalletConnect.Model.DeletedSession -> SampleDappEvents.Disconnect
                else -> SampleDappEvents.NoAction
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
                navigationChannel.trySend(SampleDappEvents.PingSuccess(pingSuccess.topic))
            }

            override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                navigationChannel.trySend(SampleDappEvents.PingError)
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

        navigationChannel.trySend(SampleDappEvents.Disconnect)
    }
}