package com.walletconnect.responder.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.auth.client.Auth
import com.walletconnect.responder.domain.ResponderDelegate
import com.walletconnect.responder.domain.mapOfAccounts1
import com.walletconnect.responder.domain.mapOfAccounts2
import com.walletconnect.responder.ui.events.ResponderEvents
import com.walletconnect.sample_common.Chains
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class AccountsViewModel : ViewModel() {
    private val _accountUI: MutableStateFlow<List<AccountsUI>> = MutableStateFlow(INITIAL_ACCOUNTS_LIST)
    val accountUI: StateFlow<List<AccountsUI>> = _accountUI.asStateFlow()

    private val _navigation = Channel<ResponderEvents>(Channel.BUFFERED)
    val navigation: Flow<ResponderEvents> = _navigation.receiveAsFlow()

    init {
        ResponderDelegate.wcEvents.map { walletEvent: Auth.Events ->
            when (walletEvent) {
                is Auth.Events.AuthResponse -> ResponderEvents.OnRequest(walletEvent.id, walletEvent.response)
                else -> ResponderEvents.NoAction
            }
        }.onEach { event ->
            _navigation.trySend(event)
        }.launchIn(viewModelScope)
    }

    //todo: Reimplement.
    fun pair(pairingUri: String) {
//        val pairingParams = Sign.Params.Pair(pairingUri)
//        SignClient.pair(pairingParams) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }
//
//        val selectedAccountInfoSet: Set<Pair<String, String>> = _accountUI.value.first { it.isSelected }.chainAddressList.map { it.chainName to it.accountAddress }.toSet()
//        val allAccountsMappedToUIDomainSetWAccountId: Map<Set<Pair<String, String>>, Int> = mapOfAllAccounts.map { (accountsId: Int, mapOfAccounts: Map<Chains, String>) ->
//            mapOfAccounts.map { it.key.chainName to it.value }.toSet() to accountsId
//        }.toMap()
//        val uiDomainSetWAccountIdsMatchingSelectedAccounts: Map<Set<Pair<String, String>>, Int> = allAccountsMappedToUIDomainSetWAccountId.filter { (uiDomainMappedSet: Set<Pair<String, String>>, _) ->
//            uiDomainMappedSet.all(selectedAccountInfoSet::contains)
//        }
//        val selectedChainAddressId: Int = uiDomainSetWAccountIdsMatchingSelectedAccounts.values.first()
//
//        WalletDelegate.setSelectedAccount(selectedChainAddressId)
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
        val INITIAL_CHAIN_ADDRESS_LIST_2: List<ChainAddressUI> = mapOfAccounts2.map { (chain: Chains, accountAddress: String) ->
            ChainAddressUI(chain.icon, chain.chainName, accountAddress)
        }
        val INITIAL_ACCOUNTS_LIST = listOf(
            AccountsUI(true, "Account 1", INITIAL_CHAIN_ADDRESS_LIST_1),
            AccountsUI(false, "Account 2", INITIAL_CHAIN_ADDRESS_LIST_2)
        )
    }
}