package com.walletconnect.wallet.ui.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.push.common.Push
import com.walletconnect.sign.client.Sign
import com.walletconnect.wallet.domain.PushWalletDelegate
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class WalletSampleViewModel : ViewModel() {
    val signEvents = WalletDelegate.wcEventModels.map { wcEvent ->
        when (wcEvent) {
            is Sign.Model.SessionProposal -> SampleWalletEvents.SessionProposal
            is Sign.Model.SessionRequest -> {
                val topic = wcEvent.topic
                val icon = wcEvent.peerMetaData?.icons?.firstOrNull()
                val peerName = wcEvent.peerMetaData?.name
                val requestId = wcEvent.request.id.toString()
                val params = wcEvent.request.params
                val chain = wcEvent.chainId
                val method = wcEvent.request.method
                val arrayOfArgs: ArrayList<String?> = arrayListOf(topic, icon, peerName, requestId, params, chain, method)

                SampleWalletEvents.SessionRequest(arrayOfArgs, arrayOfArgs.size)
            }
            else -> SampleWalletEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val pushEvents = PushWalletDelegate.wcPushEventModels.map { pushEvent ->
        when (pushEvent) {
            is Push.Wallet.Event.Request -> {
                val requestId = pushEvent.id.toString()
                val peerName = pushEvent.metadata.name
                val peerDesc = pushEvent.metadata.description
                val icon = pushEvent.metadata.icons.firstOrNull()
                val arrayOfArgs: ArrayList<String?> = arrayListOf(requestId, peerName, peerDesc, icon)

                SampleWalletEvents.PushRequest(arrayOfArgs, arrayOfArgs.size)
            }
            is Push.Wallet.Event.Message -> {
                SampleWalletEvents.PushMessage(pushEvent.message.title, pushEvent.message.body, pushEvent.message.icon, pushEvent.message.url)
            }
            is Push.Wallet.Event.Delete -> {
                SampleWalletEvents.NoAction
            }
            else -> SampleWalletEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}