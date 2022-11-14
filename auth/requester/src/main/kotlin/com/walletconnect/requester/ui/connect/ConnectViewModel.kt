package com.walletconnect.requester.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
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
import java.lang.IllegalStateException

class ConnectViewModel : ViewModel() {

    private val _navigation = Channel<RequesterEvents>(Channel.BUFFERED)
    val navigation: Flow<RequesterEvents> = _navigation.receiveAsFlow()

    var isConnectionAvailable: Boolean = false

    init {
        RequesterDelegate.wcEvents.map { walletEvent: Auth.Event ->
            when (walletEvent) {
                is Auth.Event.AuthResponse -> {
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

        RequesterDelegate.wcEvents.map { walletEvent: Auth.Event ->
            if (walletEvent is Auth.Event.ConnectionStateChange) {
                isConnectionAvailable = (walletEvent.state.isAvailable)
            }
        }.launchIn(viewModelScope)
    }

    //    fun anySettledPairingExist(): Boolean = AuthClient.getListOfSettledPairings().isNotEmpty()
    //todo: Reimplement. Right now only for demo purposes. Also remove todo at function call
    fun anySettledPairingExist(): Boolean = false

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (uri: String) -> Unit = {}) {
        val pairing: Core.Model.Pairing = if (pairingTopicPosition > -1) {
            CoreClient.Pairing.getPairings()[pairingTopicPosition]
        } else {
            CoreClient.Pairing.create() { error ->
                throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
            }!!
        }

        val requestParams = Auth.Params.Request(
            pairing = pairing,
            chainId = Chains.ETHEREUM_MAIN.chainId,
            domain = "kotlin.requester.walletconnect.com",
            nonce = randomNonce(),
            aud = "https://kotlin.requester.walletconnect.com/login",
            type = null,
            nbf = null,
            exp = null,
            statement = "Sign in with wallet.",
            requestId = null,
            resources = null,
        )

        AuthClient.request(requestParams,
            onSuccess = {
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