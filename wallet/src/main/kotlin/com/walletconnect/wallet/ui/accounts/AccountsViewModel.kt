package com.walletconnect.wallet.ui.accounts

import android.util.Log
import androidx.lifecycle.ViewModel
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAccounts1
import com.walletconnect.wallet.domain.mapOfAccounts2
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.AuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountsViewModel : ViewModel() {
    private val _accountUI: MutableStateFlow<List<AccountsUI>> = MutableStateFlow(INITIAL_ACCOUNTS_LIST)
    val accountUI: StateFlow<List<AccountsUI>> = _accountUI.asStateFlow()

    fun pair(pairingUri: String) {
        val pairingParams = WalletConnect.Params.Pair(pairingUri)
        AuthClient.pair(pairingParams) { error -> Log.e(tag(this), error.throwable.stackTraceToString()) }

        val selectedAccountInfoSet: Set<Pair<String, String>> = _accountUI.value.first { it.isSelected }.chainAddressList.map { it.chainName to it.accountAddress }.toSet()
        val allAccountsMappedToUIDomainSetWAccountId: Map<Set<Pair<String, String>>, Int> = mapOfAllAccounts.map { (accountsId: Int, mapOfAccounts: Map<EthTestChains, String>) ->
            mapOfAccounts.map { it.key.chainName to it.value }.toSet() to accountsId
        }.toMap()
        val uiDomainSetWAccountIdsMatchingSelectedAccounts: Map<Set<Pair<String, String>>, Int> = allAccountsMappedToUIDomainSetWAccountId.filter { (uiDomainMappedSet: Set<Pair<String, String>>, _) ->
            uiDomainMappedSet.all(selectedAccountInfoSet::contains)
        }
        val selectedChainAddressId: Int = uiDomainSetWAccountIdsMatchingSelectedAccounts.values.first()

        WalletDelegate.setSelectedAccount(selectedChainAddressId)
    }

    fun newAccountClicked(selectedAccountIndex: Int) {
        _accountUI.value = INITIAL_ACCOUNTS_LIST.mapIndexed { index, accountsUI ->
            accountsUI.copy(isSelected = (selectedAccountIndex == index))
        }
    }

    private companion object {
        val INITIAL_CHAIN_ADDRESS_LIST_1: List<ChainAddressUI> = mapOfAccounts1.map { (chain: EthTestChains, accountAddress: String) ->
            ChainAddressUI(chain.icon, chain.chainName, accountAddress)
        }
        val INITIAL_CHAIN_ADDRESS_LIST_2: List<ChainAddressUI> = mapOfAccounts2.map { (chain: EthTestChains, accountAddress: String) ->
            ChainAddressUI(chain.icon, chain.chainName, accountAddress)
        }
        val INITIAL_ACCOUNTS_LIST = listOf(
            AccountsUI(true, "Account 1", INITIAL_CHAIN_ADDRESS_LIST_1),
            AccountsUI(false, "Account 2", INITIAL_CHAIN_ADDRESS_LIST_2)
        )
    }
}