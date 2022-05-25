package com.walletconnect.dapp.ui.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.AuthClient
import com.walletconnect.walletconnectv2.client.WalletConnect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class DappViewModel : ViewModel() {

    val emittedEvents: Flow<SampleDappEvents>  = DappDelegate.wcEventModels.map { walletEvent: WalletConnect.Model? ->
        when (walletEvent) {
            is WalletConnect.Model.SessionEvent -> SampleDappEvents.SessionEvent(name = walletEvent.name, data = walletEvent.data)
            else -> SampleDappEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun disconnect() {
        DappDelegate.selectedSessionTopic?.let {
            val disconnectParams = WalletConnect.Params.Disconnect(sessionTopic = it, reason = "shutdown", reasonCode = 400)
            AuthClient.disconnect(disconnectParams) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
        }
    }
}