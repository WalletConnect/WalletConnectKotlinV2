package com.walletconnect.wallet.ui.sessions.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.ui.SampleWalletEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class ActiveSessionViewModel : ViewModel() {
    private val _activeSessionEvents = Channel<SampleWalletEvents>(Channel.BUFFERED)
    val activeSessionEvents = _activeSessionEvents.receiveAsFlow()

    val activeSessionUI = WalletDelegate.wcEventModels.map { walletEvent: WalletConnect.Model? ->
        when (walletEvent) {
            is WalletConnect.Model.DeletedSession.Success -> SampleWalletEvents.UpdateSessions(getLatestActiveSessions())
            is WalletConnect.Model.SettledSessionResponse -> SampleWalletEvents.UpdateSessions(getLatestActiveSessions())
            else -> SampleWalletEvents.NoAction
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SampleWalletEvents.ActiveSessions(getLatestActiveSessions()))

    private fun getLatestActiveSessions(): List<ActiveSessionUI> {
        return WalletConnectClient.getListOfSettledSessions().filter { wcSession ->
            wcSession.metaData != null
        }.map { wcSession ->
            ActiveSessionUI(
                icon = wcSession.metaData!!.icons.first(),
                name = wcSession.metaData!!.name,
                url = wcSession.metaData!!.url,
                topic = wcSession.topic
            )
        }
    }
}