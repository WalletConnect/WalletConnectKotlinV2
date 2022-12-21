package com.walletconnect.wallet.ui.host

import android.util.Log
import androidx.core.app.NotificationCompat
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

    // TODO: Tried combining the sign and push events, but push events would never propagate. Come back to see how to combine both events
    val pushEvents = PushWalletDelegate.wcPushEventModels.map { pushEvent ->
        when (pushEvent) {
            is Push.Wallet.Event.Request -> {
                Log.e("TalhaVM", pushEvent.toString())
                val requestId = pushEvent.id.toString()
                val peerName = pushEvent.metadata.name
                val peerDesc = pushEvent.metadata.description
                val icon = pushEvent.metadata.icons.firstOrNull()
                val arrayOfArgs: ArrayList<String?> = arrayListOf(requestId, peerName, peerDesc, icon)

                SampleWalletEvents.PushRequest(arrayOfArgs, arrayOfArgs.size)
            }
            is Push.Wallet.Event.Message -> {
                SampleWalletEvents.PushMessage(pushEvent.title, pushEvent.body, pushEvent.icon, pushEvent.url)
            }
            is Push.Wallet.Event.Delete -> {
                Log.e("TalhaVM", pushEvent.toString())
                SampleWalletEvents.NoAction
            }
            else -> SampleWalletEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}