package com.walletconnect.dapp.ui.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class DappViewModel : ViewModel() {

    val emittedEvents: Flow<SampleDappEvents> = DappDelegate.wcEventModels.map { walletEvent: Sign.Model? ->
        when (walletEvent) {
            is Sign.Model.SessionEvent -> SampleDappEvents.SessionEvent(name = walletEvent.name, data = walletEvent.data)
            else -> SampleDappEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun disconnect() {
        DappDelegate.selectedSessionTopic?.let {
            val disconnectParams = Sign.Params.Disconnect(sessionTopic = it)
            SignClient.disconnect(disconnectParams) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
        }
    }
}