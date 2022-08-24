package com.walletconnect.requester.ui.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.auth.client.Auth
import com.walletconnect.requester.domain.RequesterDelegate
import com.walletconnect.requester.ui.events.RequesterEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ConnectViewModel : ViewModel() {

    private val _navigation = Channel<RequesterEvents>(Channel.BUFFERED)
    val navigation: Flow<RequesterEvents> = _navigation.receiveAsFlow()

    init {
        RequesterDelegate.wcEvents.map { walletEvent: Auth.Events ->
            when (walletEvent) {
                is Auth.Events.AuthResponse -> {
                    when (walletEvent.response) {
                        is Auth.Model.Response.Result ->
                            RequesterEvents.OnApprove((walletEvent.response as Auth.Model.Response.Result).cacao)
                        is Auth.Model.Response.Error ->
                            RequesterEvents.OnReject((walletEvent.response as Auth.Model.Response.Error).code, (walletEvent.response as Auth.Model.Response.Error).message)
                    }
                }
                else -> RequesterEvents.NoAction
            }
        }.onEach { event ->
            _navigation.trySend(event)
        }.launchIn(viewModelScope)
    }

    //todo: Reimplement. Right now only for demo purposes

    //    fun anySettledPairingExist(): Boolean = AuthClient.getListOfSettledPairings().isNotEmpty()
    fun anySettledPairingExist(): Boolean = false

    //todo: Reimplement. Right now only for demo purposes
    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: () -> Unit = {}) {
        onProposedSequence()
//        val connectParams = Auth.Params.Pair(pairingTopic = pairingTopic)
//
//        AuthClient.pair(connectParams,
//            onError = { error ->
//                Log.e(tag(this@ConnectViewModel), error.throwable.stackTraceToString())
//            }
//        )
    }
}