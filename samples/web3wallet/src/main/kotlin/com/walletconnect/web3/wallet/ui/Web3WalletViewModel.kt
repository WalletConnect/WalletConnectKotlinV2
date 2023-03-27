package com.walletconnect.web3.wallet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.push.common.Push
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import com.walletconnect.web3.wallet.domain.ISSUER
import com.walletconnect.web3.wallet.domain.PushWalletDelegate
import com.walletconnect.web3.wallet.domain.WCDelegate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class Web3WalletViewModel : ViewModel() {
    val walletEvents = WCDelegate.walletEvents.map { wcEvent ->
        Log.d("Web3Wallet", "VM: $wcEvent")

        when (wcEvent) {
            is Wallet.Model.SessionRequest -> {
                val topic = wcEvent.topic
                val icon = wcEvent.peerMetaData?.icons?.firstOrNull()
                val peerName = wcEvent.peerMetaData?.name
                val requestId = wcEvent.request.id.toString()
                val params = wcEvent.request.params
                val chain = wcEvent.chainId
                val method = wcEvent.request.method
                val arrayOfArgs: ArrayList<String?> = arrayListOf(topic, icon, peerName, requestId, params, chain, method)

                SignEvent.SessionRequest(arrayOfArgs, arrayOfArgs.size)
            }
            is Wallet.Model.AuthRequest -> {
                val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(wcEvent.payloadParams, ISSUER))
                    ?: throw Exception("Error formatting message")
                AuthEvent.OnRequest(wcEvent.id, message)
            }
            is Wallet.Model.SessionDelete -> SignEvent.Disconnect
            is Wallet.Model.SessionProposal -> SignEvent.SessionProposal
            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val pushEvents = PushWalletDelegate.wcPushEventModels.map { pushEvent ->
        when (pushEvent) {
            is Push.Wallet.Event.Request -> {
                val requestId = pushEvent.id.toString()
                val peerName = pushEvent.metadata.name
                val peerDesc = pushEvent.metadata.description
                val icon = pushEvent.metadata.icons.firstOrNull()
                val redirect = pushEvent.metadata.redirect

                PushRequest(requestId, peerName, peerDesc, icon, redirect)
            }
            is Push.Wallet.Event.Message -> {
                PushMessage(pushEvent.title, pushEvent.body, pushEvent.icon, pushEvent.url)
            }
            is Push.Wallet.Event.Delete -> {
                NoAction
            }
            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun pair(pairingUri: String) {
        val pairingParams = Wallet.Params.Pair(pairingUri)
        Web3Wallet.pair(pairingParams) { error -> Firebase.crashlytics.recordException(error.throwable) }
    }
}