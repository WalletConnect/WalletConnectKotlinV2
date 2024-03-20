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
import com.walletconnect.sample.wallet.ui.state.PairingEvent
import com.walletconnect.sample.wallet.ui.state.connectionStateFlow
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class Web3WalletViewModel : ViewModel() {
    private val connectivityStateFlow: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)
    val connectionState = merge(connectivityStateFlow.asStateFlow(), connectionStateFlow.asStateFlow())

    private val _eventsSharedFlow: MutableSharedFlow<PairingEvent> = MutableSharedFlow()
    val eventsSharedFlow = _eventsSharedFlow.asSharedFlow()

    private val _isLoadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoadingFlow = _isLoadingFlow.asSharedFlow()

    private val _timerFlow: MutableStateFlow<String> = MutableStateFlow("0")
    val timerFlow = _timerFlow.asStateFlow()

    private val _sessionRequestStateFlow: MutableSharedFlow<SignEvent.SessionRequest> = MutableSharedFlow()
    val sessionRequestStateFlow = _sessionRequestStateFlow.asSharedFlow()

    init {
        WCDelegate.coreEvents.onEach { coreEvent ->
            _isLoadingFlow.value = (coreEvent as? Core.Model.PairingState)?.isPairingState ?: false

            if (coreEvent is Core.Model.ExpiredPairing) {
                val pairingType = if (coreEvent.pairing.isActive) "Active" else "Inactive"
                _eventsSharedFlow.emit(PairingEvent.Expired("$pairingType pairing expired"))
            }
        }.launchIn(viewModelScope)

        flow {
            while (true) {
                emit(Unit)
                delay(1000)
            }
        }.onEach {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("en", "US"))
            _timerFlow.value = dateFormat.format(timestamp)
        }.launchIn(viewModelScope)
    }

    val walletEvents = WCDelegate.walletEvents.map { wcEvent ->
        Log.d("Web3Wallet", "VM: $wcEvent")

        when (wcEvent) {
            is Wallet.Model.ExpiredProposal -> {
                viewModelScope.launch {
                    _eventsSharedFlow.emit(PairingEvent.ProposalExpired("Proposal expired, please pair again"))
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
                if (WCDelegate.currentId != WCDelegate.sessionRequestEvent?.first?.request?.id) {
                    _sessionRequestStateFlow.emit(SignEvent.SessionRequest(arrayOfArgs, arrayOfArgs.size))
                } else {
                    println("wallet request already there: ${wcEvent.request.id}")
                }
            }

            is Wallet.Model.AuthRequest -> {
                _isLoadingFlow.value = false
                val message = Web3Wallet.formatMessage(Wallet.Params.FormatMessage(wcEvent.payloadParams, ISSUER))
                    ?: throw Exception("Error formatting message")
                AuthEvent.OnRequest(wcEvent.id, message)
            }

            is Wallet.Model.SessionAuthenticate -> {
                _isLoadingFlow.value = false
                SignEvent.SessionAuthenticate
            }

            is Wallet.Model.SessionDelete -> SignEvent.Disconnect
            is Wallet.Model.SessionProposal -> {
                _isLoadingFlow.value = false
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

    fun showLoader(isLoading: Boolean) {
        _isLoadingFlow.value = isLoading
    }

    fun pair(pairingUri: String) {
        _isLoadingFlow.value = true

        try {
            val pairingParams = Wallet.Params.Pair(pairingUri.removePrefix("kotlin-web3wallet://wc?uri="))
            Web3Wallet.pair(pairingParams) { error ->
                Firebase.crashlytics.recordException(error.throwable)
                viewModelScope.launch {
                    _isLoadingFlow.value = false
                    _eventsSharedFlow.emit(PairingEvent.Error(error.throwable.message ?: "Unexpected error happened, please contact support"))
                }
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            viewModelScope.launch {
                _isLoadingFlow.value = false
                _eventsSharedFlow.emit(PairingEvent.Error(e.message ?: "Unexpected error happened, please contact support"))
            }
        }
    }
}