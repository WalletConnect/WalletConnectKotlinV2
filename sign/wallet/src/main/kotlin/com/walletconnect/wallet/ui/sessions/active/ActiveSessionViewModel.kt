package com.walletconnect.wallet.ui.sessions.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.wallet.Wallet3Wallet
import com.walletconnect.wallet.domain.WalletDelegate
import kotlinx.coroutines.flow.*

class ActiveSessionViewModel : ViewModel() {
    val activeSessionUI: StateFlow<List<ActiveSessionUI>> = WalletDelegate.wcEventModels
        .filterNotNull()
        .map {
            getLatestActiveSessions()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), getLatestActiveSessions())

    private fun getLatestActiveSessions(): List<ActiveSessionUI> {
        return Wallet3Wallet.getListOfActiveSessions().filter { wcSession ->
            wcSession.metaData != null
        }.map { wcSession ->
            ActiveSessionUI(
                icon = wcSession.metaData?.icons?.firstOrNull(),
                name = wcSession.metaData!!.name,
                url = wcSession.metaData!!.url,
                topic = wcSession.topic
            )
        }
    }
}