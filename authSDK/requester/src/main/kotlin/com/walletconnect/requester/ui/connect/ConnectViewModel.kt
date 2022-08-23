package com.walletconnect.requester.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.AuthClient
import com.walletconnect.requester.domain.RequesterDelegate
import com.walletconnect.requester.ui.events.RequesterEvents
import com.walletconnect.requester.ui.session.CacaoStore
import com.walletconnect.requester.utils.randomNonce
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectViewModel : ViewModel() {

    private val _navigation = Channel<RequesterEvents>(Channel.BUFFERED)
    val navigation: Flow<RequesterEvents> = _navigation.receiveAsFlow()

    init {
        RequesterDelegate.wcEvents.map { walletEvent: Auth.Events ->
            when (walletEvent) {
                is Auth.Events.AuthResponse -> {
                    when (val response: Auth.Model.Response = walletEvent.response) {
                        is Auth.Model.Response.Result -> RequesterEvents.OnAuthenticated(response.cacao).also { CacaoStore.currentCacao = response.cacao }
                        is Auth.Model.Response.Error -> RequesterEvents.OnError(response.code, response.message)
                    }
                }
                else -> RequesterEvents.NoAction
            }
        }.onEach { event ->
            _navigation.trySend(event)
        }.launchIn(viewModelScope)
    }

    //todo: Reimplement. Right now only for demo purposes. Also remove todo at function call

    //    fun anySettledPairingExist(): Boolean = AuthClient.getListOfSettledPairings().isNotEmpty()
    fun anySettledPairingExist(): Boolean = false

    //todo: Reimplement pairingTopicPosition. Right now only for demo purposes
    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (uri: String) -> Unit = {}) {
        val requestParams = Auth.Params.Request(
            chainId = Chains.ETHEREUM_MAIN.chainId,
            domain = "kotlin.requester.walletconnect.com",
            nonce = randomNonce(),
            aud = "https://kotlin.requester.walletconnect.com/login",
            type = null,
            nbf = null,
            exp = null,
            statement = null,
            requestId = null,
            resources = null,
        )

        AuthClient.request(requestParams,
            onPairing = { pairing ->
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(pairing.uri)
                }
            },
            onError = { error ->
                Log.e(tag(this@ConnectViewModel), error.throwable.stackTraceToString())
            }
        )
    }

}