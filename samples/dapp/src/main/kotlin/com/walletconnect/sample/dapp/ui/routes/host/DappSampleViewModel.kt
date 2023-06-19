package com.walletconnect.sample.dapp.ui.routes.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.dapp.domain.DappDelegate
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sign.client.Sign
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class DappSampleViewModel : ViewModel() {

    val events = DappDelegate
        .wcEventModels
        .map { event ->
            when (event) {
                is Sign.Model.DeletedSession -> DappSampleEvents.Disconnect
                is Sign.Model.Session -> DappSampleEvents.SessionExtend
                is Sign.Model.ConnectionState -> DappSampleEvents.ConnectionEvent(event.isAvailable)
                is Sign.Model.Error -> DappSampleEvents.RequestError(event.throwable.localizedMessage ?: "Something goes wrong")
                else -> DappSampleEvents.NoAction
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}
