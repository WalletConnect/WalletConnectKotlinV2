package com.walletconnect.responder.ui.accounts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.auth.client.Auth
import com.walletconnect.responder.domain.ResponderDelegate
import com.walletconnect.responder.domain.mapOfAccounts1
import com.walletconnect.responder.ui.events.ResponderEvents
import com.walletconnect.responder.ui.request.RequestStore
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class AccountsViewModel : ViewModel() {
    private val _accountUI: MutableStateFlow<List<AccountsUI>> = MutableStateFlow(INITIAL_ACCOUNTS_LIST)
    val accountUI: StateFlow<List<AccountsUI>> = _accountUI.asStateFlow()

    private val _navigation = Channel<ResponderEvents>(Channel.BUFFERED)
    val navigation: Flow<ResponderEvents> = _navigation.receiveAsFlow()

    init {
        ResponderDelegate.wcEvents.map { event: Auth.Event ->
            when (event) {
                //todo: remove `.also { RequestStore.currentRequest = it }` after implementing pending request
                is Auth.Event.AuthRequest -> ResponderEvents.OnRequest(event.id, event.message).also { RequestStore.currentRequest = it }
                else -> ResponderEvents.NoAction
            }
        }.onEach { event ->
            _navigation.trySend(event)
        }.launchIn(viewModelScope)
    }

    fun pair(pairingUri: String) {
        val pairingParams = Core.Params.Pair(pairingUri)
        CoreClient.Pairing.pair(pairingParams) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }
    }

    fun newAccountClicked(selectedAccountIndex: Int) {
        _accountUI.value = INITIAL_ACCOUNTS_LIST.mapIndexed { index, accountsUI ->
            accountsUI.copy(isSelected = (selectedAccountIndex == index))
        }
    }

    private companion object {
        val INITIAL_CHAIN_ADDRESS_LIST_1: List<ChainAddressUI> = mapOfAccounts1.map { (chain: Chains, accountAddress: String) ->
            ChainAddressUI(chain.icon, chain.chainName, accountAddress)
        }
        val INITIAL_ACCOUNTS_LIST = listOf(
            AccountsUI(true, "Account 1", INITIAL_CHAIN_ADDRESS_LIST_1),
        )
    }
}