package com.walletconnect.dapp.ui.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class EmittedEventsViewModel : ViewModel() {

    private val _emittedEvents = Channel<SampleDappEvents>(Channel.BUFFERED)
    val emittedEvents: Flow<SampleDappEvents> = _emittedEvents.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent: WalletConnect.Model? ->
            when (walletEvent) {
                is WalletConnect.Model.SessionEvent -> SampleDappEvents.SessionEvent(name = walletEvent.name, data = walletEvent.data)
                else -> SampleDappEvents.NoAction
            }
        }.onEach { event ->
            _emittedEvents.trySend(event)
        }.launchIn(viewModelScope)
    }
}