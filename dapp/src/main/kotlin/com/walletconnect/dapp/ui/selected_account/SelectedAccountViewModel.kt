package com.walletconnect.dapp.ui.selected_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.*
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class SelectedAccountViewModel : ViewModel() {
    private val _dappEvents: MutableStateFlow<Pair<Long, WalletConnect.Model?>> = MutableStateFlow(Pair(0, null))
    val navigation: LiveData<NavigationEvents> =
        DappDelegate.wcEventModels.combine(_dappEvents) { (walletEventTimestamp, walletEvent: WalletConnect.Model?), (dappEventTimestamp, dappEvent: WalletConnect.Model?) ->
            if (dappEventTimestamp > walletEventTimestamp) {
                when (dappEvent) {
                    is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
                    is WalletConnect.Model.Error -> NavigationEvents.RequestError(dappEvent.error.localizedMessage ?: "Error trying to send request")
                    else -> NavigationEvents.NoAction
                }
            } else {
                when (walletEvent) {
                    is WalletConnect.Model.UpgradedSession -> {
                        val selectedAccountUI = getSelectedAccount()
                        NavigationEvents.UpgradedSelectedAccountUI(selectedAccountUI)
                    }
                    is WalletConnect.Model.SessionPayloadResponse -> {
                        when (walletEvent.result) {
                            is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> NavigationEvents.RequestSuccess((walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcResult).result)
                            is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> {
                                val errorResult = (walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcError)
                                NavigationEvents.RequestPeerError("Error Message: ${errorResult.message}\n Error Code: ${errorResult.code}")
                            }
                            else -> NavigationEvents.NoAction
                        }
                    }
                    is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
                    else -> NavigationEvents.NoAction
                }
            }
        }.asLiveData(viewModelScope.coroutineContext)

    fun requestMethod(method: String) {
        val (parentChain, chainId, account) = requireNotNull(DappDelegate.selectedAccountDetails)

        val params: String = when {
            method.equals("personal_sign", true) -> getPersonalSignBody(account)
            method.equals("eth_sendTransaction", true) -> getEthSendTransaction(account)
            method.equals("eth_signTypedData", true) -> getEthSignTypedData(account)
            else -> "[]"
        }

        val requestParams = WalletConnect.Params.Request(
            sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic),
            method = method,
            params = params, // stringified JSON
            chainId = "$parentChain:$chainId"
        )

        WalletConnectClient.request(requestParams) {
            _dappEvents.tryEmit(System.currentTimeMillis() to it)
        }
    }

    fun getSelectedAccount(selectAccountDetails: String? = null): SelectedAccountUI {
        if (selectAccountDetails != null) {
            DappDelegate.setSelectedAccountDetails(selectAccountDetails)
        }

        val (parentChain, chainId, account) = requireNotNull(DappDelegate.selectedAccountDetails)
        val chainDetails = Chains.values().first {
            it.parentChain == parentChain && it.chainId == chainId.toInt()
        }
        val listOfMethods = WalletConnectClient.getListOfSettledSessions().filter {
            it.topic == DappDelegate.selectedSessionTopic
        }.map {
            it.permissions.jsonRpc.methods
        }.flatten()

        return SelectedAccountUI(chainDetails.icon, chainDetails.chainName, account, listOfMethods)
    }
}