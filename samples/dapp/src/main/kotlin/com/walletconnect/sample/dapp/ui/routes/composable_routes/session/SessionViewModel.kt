package com.walletconnect.sample.dapp.ui.routes.composable_routes.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.dapp.client.PushDappClient
import com.walletconnect.sample.dapp.domain.DappDelegate
import com.walletconnect.sample.dapp.domain.PushDappDelegate
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.flow.*
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
                    is Sign.Model.UpdatedSession -> {
                        _sessionUI.value = getSessions(event.topic)
                    }
                    is Sign.Model.DeletedSession -> {
                        _sessionEvents.emit(DappSampleEvents.Disconnect)
                    }
                    else -> Unit
                }
            }.launchIn(viewModelScope)
    }

    private fun getSessions(topic: String? = null): List<SessionUi> {
        return SignClient.getListOfActiveSessions().filter {
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
        val pingParams = Sign.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        SignClient.ping(pingParams, object : Sign.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                viewModelScope.launch {
                    _sessionEvents.emit(DappSampleEvents.PingSuccess(pingSuccess.topic))
                }
            }

            override fun onError(pingError: Sign.Model.Ping.Error) {
                viewModelScope.launch {
                    _sessionEvents.emit(DappSampleEvents.PingError)
                }
            }
        })
    }

    fun disconnect() {
        if (DappDelegate.selectedSessionTopic != null) {
            val disconnectParams =
                Sign.Params.Disconnect(sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic))

            SignClient.disconnect(disconnectParams) { error ->
                Timber.tag(tag(this)).e(error.throwable.stackTraceToString())
            }
            DappDelegate.deselectAccountDetails()

            PushDappClient.getActiveSubscriptions().entries.firstOrNull()?.value?.topic?.let { pushTopic ->
                PushDappClient.deleteSubscription(Push.Dapp.Params.Delete(pushTopic)) { error ->
                    Timber.tag(tag(this)).e(error.throwable.stackTraceToString())
                }
            }
        }

        viewModelScope.launch {
            _sessionEvents.emit(DappSampleEvents.Disconnect)
        }
    }

    fun pushRequest() {
        val pairingTopic = CoreClient.Pairing.getPairings().map { it.topic }.first { pairingTopic ->
            SignClient.getListOfActiveSessions()
                .any { session -> session.pairingTopic == pairingTopic }
        }
        val ethAccount = SignClient.getListOfActiveSessions().first { session ->
            session.pairingTopic == pairingTopic
        }.namespaces.entries.first().value.accounts.first()

        PushDappClient.request(Push.Dapp.Params.Request(ethAccount, pairingTopic),
            { pushRequestId ->
                Timber.tag(tag(this)).e("Request sent with id " + pushRequestId.id)
            }, {
                Timber.tag(tag(this)).e(it.throwable.stackTraceToString())
            })
    }

    fun pushNotify() {
        val pushTopic = PushDappDelegate.activePushSubscription?.topic
            ?: PushDappClient.getActiveSubscriptions().keys.firstOrNull() ?: return
        val pushMessage = Push.Model.Message(
            "Kotlin Dapp Title",
            "Kotlin Dapp Body",
            "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
            "https://walletconnect.com"
        )
        val notifyParams = Push.Dapp.Params.Notify(pushTopic, pushMessage)

        PushDappClient.notify(notifyParams) { error ->
            Timber.tag(tag(this)).e(error.throwable.stackTraceToString())
        }
    }

}