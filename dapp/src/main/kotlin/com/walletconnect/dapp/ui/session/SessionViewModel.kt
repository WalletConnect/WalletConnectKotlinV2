package com.walletconnect.dapp.ui.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.EthChains
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.client.SignClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val _sessionUI: MutableStateFlow<List<SessionUI>> = MutableStateFlow(getSessions())
    val uiState: StateFlow<List<SessionUI>> = _sessionUI.asStateFlow()

    private val _navigationEvents: MutableSharedFlow<SampleDappEvents> = MutableSharedFlow()
    val navigationEvents: SharedFlow<SampleDappEvents> = _navigationEvents.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { walletEvent ->
                when (walletEvent) {
                    is Sign.Model.UpdatedSession -> {
                        _sessionUI.value = getSessions(walletEvent.topic)
                    }
                    is Sign.Model.DeletedSession -> {
                        _navigationEvents.emit(SampleDappEvents.Disconnect)
                    }
                    else -> Unit
                }
            }.launchIn(viewModelScope)

    }

    private fun getSessions(topic: String? = null): List<SessionUI> {
        return SignClient.getListOfSettledSessions().filter {
            if (topic != null) {
                it.topic == topic
            } else {
                it.topic == DappDelegate.selectedSessionTopic
            }
        }.flatMap { settledSession ->
            settledSession.namespaces.values.flatMap { it.accounts }
        }.map { caip10Account ->
            val (chainNamespace, chainReference, account) = caip10Account.split(":")
            val chain = EthChains.values().first { chain ->
                chain.chainNamespace == chainNamespace && chain.chainReference == chainReference.toInt()
            }

            SessionUI(chain.icon, chain.name, account, chain.chainNamespace, chain.chainReference)
        }
    }

    fun ping() {
        val pingParams = Sign.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        SignClient.ping(pingParams, object : Sign.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                viewModelScope.launch {
                    _navigationEvents.emit(SampleDappEvents.PingSuccess(pingSuccess.topic))
                }
            }

            override fun onError(pingError: Sign.Model.Ping.Error) {
                viewModelScope.launch {
                    _navigationEvents.emit(SampleDappEvents.PingError)
                }
            }
        })
    }

    fun disconnect() {
        if (DappDelegate.selectedSessionTopic != null) {
            val disconnectParams = Sign.Params.Disconnect(sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic))

            SignClient.disconnect(disconnectParams) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
            DappDelegate.deselectAccountDetails()
        }

        viewModelScope.launch {
            _navigationEvents.emit(SampleDappEvents.Disconnect)
        }
    }
}