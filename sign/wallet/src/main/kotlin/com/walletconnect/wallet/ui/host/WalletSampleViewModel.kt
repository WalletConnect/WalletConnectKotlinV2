package com.walletconnect.wallet.ui.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sign.client.Sign
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.ui.SampleSignEvents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class WalletSampleViewModel : ViewModel() {
    val events = WalletDelegate.wcEventModels.map { wcEvent ->
        when (wcEvent) {
            is Sign.Model.SessionProposal -> SampleSignEvents.SessionProposal
            is Sign.Model.SessionRequest -> {
                val topic = wcEvent.topic
                val icon = wcEvent.peerMetaData?.icons?.firstOrNull()
                val peerName = wcEvent.peerMetaData?.name
                val requestId = wcEvent.request.id.toString()
                val params = wcEvent.request.params
                val chain = wcEvent.chainId
                val method = wcEvent.request.method
                val arrayOfArgs: ArrayList<String?> = arrayListOf(topic, icon, peerName, requestId, params, chain, method)

                SampleSignEvents.SessionRequest(arrayOfArgs, arrayOfArgs.size)
            }
            else -> SampleSignEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}