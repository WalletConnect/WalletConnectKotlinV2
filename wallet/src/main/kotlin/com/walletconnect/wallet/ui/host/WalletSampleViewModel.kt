package com.walletconnect.wallet.ui.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.ui.SampleWalletEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class WalletSampleViewModel : ViewModel() {
    val events = WalletDelegate.wcEventModels.map { wcEvent ->
        when (wcEvent) {
            is WalletConnect.Model.SessionProposal -> SampleWalletEvents.SessionProposal
            else -> SampleWalletEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}