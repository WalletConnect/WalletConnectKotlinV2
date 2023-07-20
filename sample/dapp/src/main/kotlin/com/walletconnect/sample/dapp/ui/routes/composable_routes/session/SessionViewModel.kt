package com.walletconnect.sample.dapp.ui.routes.composable_routes.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.common.Chains
import com.walletconnect.sample.common.tag
import com.walletconnect.sample.dapp.domain.DappDelegate
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionViewModel : ViewModel() {

    private val _sessionUI: MutableStateFlow<List<SessionUi>> = MutableStateFlow(getSessions())
    val uiState: StateFlow<List<SessionUi>> = _sessionUI.asStateFlow()

    private val _sessionEvents: MutableSharedFlow<DappSampleEvents> = MutableSharedFlow()
    val sessionEvent: SharedFlow<DappSampleEvents>
        get() = _sessionEvents.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { event ->
                when (event) {
                    is Modal.Model.UpdatedSession -> {
                        _sessionUI.value = getSessions(event.topic)
                    }
                    is Modal.Model.DeletedSession -> {
                        _sessionEvents.emit(DappSampleEvents.Disconnect)
                    }
                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun getSessions(topic: String? = null): List<SessionUi> {
        return WalletConnectModal.getListOfActiveSessions().filter {
            if (topic != null) {
                it.topic == topic
            } else {
                it.topic == DappDelegate.selectedSessionTopic
            }
        }.flatMap { settledSession ->
            settledSession.namespaces.values.flatMap { it.accounts }
        }.map { caip10Account ->
            val (chainNamespace, chainReference, account) = caip10Account.split(":")
            val chain = Chains.values().first { chain ->
                chain.chainNamespace == chainNamespace && chain.chainReference == chainReference
            }

            SessionUi(chain.icon, chain.name, account, chain.chainNamespace, chain.chainReference)
        }
    }

    fun ping() {
        val pingParams = Modal.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        WalletConnectModal.ping(pingParams, object : Modal.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: Modal.Model.Ping.Success) {
                viewModelScope.launch {
                    _sessionEvents.emit(DappSampleEvents.PingSuccess(pingSuccess.topic))
                }
            }

            override fun onError(pingError: Modal.Model.Ping.Error) {
                viewModelScope.launch {
                    _sessionEvents.emit(DappSampleEvents.PingError)
                }
            }
        })
    }

    fun disconnect() {
        if (DappDelegate.selectedSessionTopic != null) {
            val disconnectParams =
                Modal.Params.Disconnect(sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic))

            WalletConnectModal.disconnect(disconnectParams) { error ->
                Timber.tag(tag(this)).e(error.throwable.stackTraceToString())
            }
            DappDelegate.deselectAccountDetails()
        }

        viewModelScope.launch {
            _sessionEvents.emit(DappSampleEvents.Disconnect)
        }
    }

}