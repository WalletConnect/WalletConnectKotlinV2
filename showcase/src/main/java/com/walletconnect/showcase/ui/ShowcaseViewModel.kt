package com.walletconnect.showcase.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sample_common.tag
import com.walletconnect.showcase.domain.ISSUER
import com.walletconnect.showcase.domain.WCDelegate
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class ShowcaseViewModel(private val application: Application) : ViewModel() {
    val walletEvents = WCDelegate.walletEvents.map { wcEvent ->
        Log.e("Web3Wallet", "VM: $wcEvent")

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
                val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(wcEvent.payloadParams, ISSUER)) ?: throw Exception("Error formatting message")
                AuthEvent.OnRequest(wcEvent.id, message)
            }
            is Wallet.Model.SessionDelete -> SignEvent.Disconnect
            is Wallet.Model.SessionProposal -> SignEvent.SessionProposal
            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun pair(pairingUri: String) {
        val pairingParams = Core.Params.Pair(pairingUri)
        CoreClient.Pairing.pair(pairingParams) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }
    }
}