package com.walletconnect.sample.wallet.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.InvalidProjectIdException
import com.walletconnect.android.internal.common.exception.ProjectIdDoesNotExistException
import com.walletconnect.sample.wallet.domain.ISSUER
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.sample.wallet.ui.state.ConnectionState
import com.walletconnect.sample.wallet.ui.state.PairingEvent
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Web3WalletViewModel : ViewModel() {
    private val connectivityStateFlow: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Idle)
    val connectionState = connectivityStateFlow.asStateFlow()

    private val _eventsSharedFlow: MutableSharedFlow<PairingEvent> = MutableSharedFlow()
    val eventsSharedFlow = _eventsSharedFlow.asSharedFlow()

    private val _isLoadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoadingFlow = _isLoadingFlow.asSharedFlow()

    private val _isRequestLoadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRequestLoadingFlow = _isRequestLoadingFlow.asSharedFlow()

    private val _timerFlow: MutableStateFlow<String> = MutableStateFlow("0")
    val timerFlow = _timerFlow.asStateFlow()

    private val _sessionRequestStateFlow: MutableSharedFlow<SignEvent.SessionRequest> = MutableSharedFlow()
    val sessionRequestStateFlow = _sessionRequestStateFlow.asSharedFlow()

    init {
        WCDelegate.coreEvents.onEach { coreEvent ->
            _isLoadingFlow.value = (coreEvent as? Core.Model.PairingState)?.isPairingState ?: false
        }.launchIn(viewModelScope)

        flow {
            while (true) {
                emit(Unit)
                delay(1000)
            }
        }.onEach {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            _timerFlow.value = dateFormat.format(timestamp)
        }.launchIn(viewModelScope)

        WCDelegate.connectionState.onEach {
            val connectionState = if (it.isAvailable) {
                ConnectionState.Ok
            } else {
                val message = when (it.reason) {
                    is Wallet.Model.ConnectionState.Reason.ConnectionFailed -> {
                        if ((it.reason as Wallet.Model.ConnectionState.Reason.ConnectionFailed).throwable is ProjectIdDoesNotExistException ||
                            (it.reason as Wallet.Model.ConnectionState.Reason.ConnectionFailed).throwable is InvalidProjectIdException
                        ) "Invalid Project Id" else "Connection failed"
                    }

                    else -> "Connection closed"
                }

                ConnectionState.Error(message)
            }
            connectivityStateFlow.value = connectionState
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

            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    fun showLoader(isLoading: Boolean) {
        _isLoadingFlow.value = isLoading
    }

    fun showRequestLoader(isLoading: Boolean) {
        if (_isRequestLoadingFlow.value != isLoading) {
            _isRequestLoadingFlow.value = isLoading
        }
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