package com.walletconnect.sample.wallet.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.notify.client.Notify
import com.walletconnect.sample.wallet.domain.NotificationHandler
import com.walletconnect.sample.wallet.domain.ISSUER
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.PairingState
import com.walletconnect.sample.wallet.ui.state.connectionStateFlow
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class Web3WalletViewModel(_app: Application) : AndroidViewModel(_app) {
    private val connectivityStateFlow: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)
    val connectionState = merge(connectivityStateFlow.asStateFlow(), connectionStateFlow.asStateFlow())

    private val _pairingStateSharedFlow: MutableSharedFlow<PairingState> = MutableSharedFlow()
    val pairingStateSharedFlow = _pairingStateSharedFlow.asSharedFlow()

    init {
        NotifyDelegate.notifyEvents
            .filterIsInstance<Notify.Event.Message>()
            .onEach { message -> NotificationHandler.showNotification(message.message.message, getApplication()) }
            .launchIn(viewModelScope)
    }

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
                viewModelScope.launch {
                    _pairingStateSharedFlow.emit(PairingState.Success)
                }
                val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(wcEvent.payloadParams, ISSUER))
                    ?: throw Exception("Error formatting message")
                AuthEvent.OnRequest(wcEvent.id, message)
            }

            is Wallet.Model.SessionDelete -> SignEvent.Disconnect
            is Wallet.Model.SessionProposal -> {
                viewModelScope.launch {
                    _pairingStateSharedFlow.emit(PairingState.Success)
                }
                SignEvent.SessionProposal
            }

            is Wallet.Model.ConnectionState -> {
                val connectionState = if (wcEvent.isAvailable) {
                    ConnectionState.Ok
                } else {
                    ConnectionState.Error("No Internet connection, please check your internet connection and try again")
                }
                connectivityStateFlow.emit(connectionState)
            }

            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun pair(pairingUri: String) {
        viewModelScope.launch {
            _pairingStateSharedFlow.emit(PairingState.Loading)
        }

        val pairingParams = Wallet.Params.Pair(pairingUri)
        Web3Wallet.pair(pairingParams) { error ->
            Firebase.crashlytics.recordException(error.throwable)
            viewModelScope.launch {
                _pairingStateSharedFlow.emit(PairingState.Error(error.throwable.message ?: ""))
            }
        }
    }
}