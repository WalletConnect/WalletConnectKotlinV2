package com.walletconnect.sample.wallet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.sample.wallet.domain.ISSUER
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class Web3WalletViewModel : ViewModel() {
    private val connectivityStateFlow: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)
    val connectionState = merge(connectivityStateFlow.asStateFlow(), connectionStateFlow.asStateFlow())

    private val _pairingSharedFlow: MutableSharedFlow<PairingState> = MutableSharedFlow()
    val pairingSharedFlow = _pairingSharedFlow.asSharedFlow()

    init {
        WCDelegate.coreEvents.onEach { coreEvent ->
            if (coreEvent is Core.Model.ExpiredPairing) {
                val pairingType = if (coreEvent.pairing.isActive) "Active" else "Inactive"
                _pairingSharedFlow.emit(PairingState.Expired("$pairingType pairing expired"))
            } else if (coreEvent is Core.Model.PairingState) {
                println("kobe: checking emit pairing loading")
                if (coreEvent.isPairingState) {
                    println("kobe: emit pairing loading")
                    _pairingSharedFlow.emit(PairingState.Loading)
                }
            }
        }.launchIn(viewModelScope)
    }

    val walletEvents = WCDelegate.walletEvents.map { wcEvent ->
        Log.d("Web3Wallet", "VM: $wcEvent")

        when (wcEvent) {
            is Wallet.Model.ExpiredProposal -> {
                viewModelScope.launch {
                    _pairingSharedFlow.emit(PairingState.ProposalExpired("Proposal expired, please pair again"))
                }

            }

            is Wallet.Model.ExpiredRequest -> SignEvent.ExpiredRequest
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
                    _pairingSharedFlow.emit(PairingState.Success)
                }
                val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(wcEvent.payloadParams, ISSUER))
                    ?: throw Exception("Error formatting message")
                AuthEvent.OnRequest(wcEvent.id, message)
            }

            is Wallet.Model.SessionDelete -> SignEvent.Disconnect
            is Wallet.Model.SessionProposal -> {
                viewModelScope.launch {
                    _pairingSharedFlow.emit(PairingState.Success)
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
            _pairingSharedFlow.emit(PairingState.Loading)
        }

        try {
            val pairingParams = Wallet.Params.Pair(pairingUri)
            Web3Wallet.pair(pairingParams) { error ->
                Firebase.crashlytics.recordException(error.throwable)
                viewModelScope.launch {
                    _pairingSharedFlow.emit(PairingState.Error(error.throwable.message ?: "Unexpected error happened, please contact support"))
                }
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            viewModelScope.launch {
                _pairingSharedFlow.emit(PairingState.Error(e.message ?: "Unexpected error happened, please contact support"))
            }
        }
    }
}