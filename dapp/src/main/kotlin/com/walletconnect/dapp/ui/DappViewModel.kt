package com.walletconnect.dapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.*
import com.walletconnect.dapp.ui.chain_select.ChainSelectionUI
import com.walletconnect.dapp.ui.selected_account.SelectedAccountUI
import com.walletconnect.dapp.ui.session.SessionUI
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class DappViewModel : ViewModel() {
    private val _dappEvents: MutableStateFlow<WalletConnect.Model?> = MutableStateFlow(null)
    val navigation: LiveData<NavigationEvents> = DappDelegate.wcEventModels.combine(_dappEvents) { walletEvent: WalletConnect.Model, dappEvent: WalletConnect.Model? ->
        when {
            walletEvent is WalletConnect.Model.ApprovedSession -> NavigationEvents.ToSession
            dappEvent is WalletConnect.Model.Ping.Success -> NavigationEvents.PingSuccess(dappEvent.topic)
            dappEvent is WalletConnect.Model.Ping.Error -> NavigationEvents.PingError
            dappEvent is WalletConnect.Model.DeletedSession -> NavigationEvents.Disconnect
            dappEvent is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> NavigationEvents.RequestSuccess(dappEvent.result)
            dappEvent is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> NavigationEvents.RequestPeerError("Error Message: ${dappEvent.errorDetails.message}\t Error Code: ${dappEvent.errorDetails.code}")
            dappEvent is WalletConnect.Model.JsonRpcResponse.Error -> NavigationEvents.RequestError(dappEvent.error.localizedMessage ?: "Error trying to send request")
            walletEvent is WalletConnect.Model.UpdatedSession -> {
                val listOfAccounts = getListOfAccounts(walletEvent.topic)
                NavigationEvents.UpdatedListOfAccounts(listOfAccounts)
            }
            walletEvent is WalletConnect.Model.UpgradedSession -> {
                val selectedAccountUI = getSelectedAccount()
                NavigationEvents.UpgradedSelectedAccountUI(selectedAccountUI)
            }
            dappEvent is WalletConnect.Model.WCException || dappEvent == null -> NavigationEvents.Error
            else -> NavigationEvents.NoAction
        }
    }.asLiveData(viewModelScope.coroutineContext)

    val listOfChainUI: List<ChainSelectionUI>
        get() = _listOfChainUI

    fun connectToWallet(pairingTopicPosition: Int = -1): String? {
        var pairingTopic: String? = null

        if (pairingTopicPosition > -1) {
            pairingTopic = WalletConnectClient.getListOfSettledPairings()[pairingTopicPosition].topic
            DappDelegate.setSelectedPairingTopicOnConnect(pairingTopic)
        }

        val selectedChains: List<ChainSelectionUI> = listOfChainUI.filter { it.isSelected }
        val blockchains = selectedChains.map { "${it.parentChain}:${it.chainId}" }
        val methods = selectedChains.map { it.methods }.flatten().distinct()
        val sessionPermissions = WalletConnect.Model.SessionPermissions(
            blockchain = WalletConnect.Model.SessionPermissions.Blockchain(chains = blockchains),
            jsonRpc = WalletConnect.Model.SessionPermissions.Jsonrpc(methods = methods),
            notification = null
        )
        val connectParams = WalletConnect.Params.Connect(permissions = sessionPermissions, pairingTopic = pairingTopic)

        return WalletConnectClient.connect(connectParams)
    }

    fun ping() {
        val pingParams = WalletConnect.Params.Ping(topic = requireNotNull(DappDelegate.selectedSessionTopic))

        WalletConnectClient.ping(pingParams, object : WalletConnect.Listeners.SessionPing {
            override fun onSuccess(pingSuccess: WalletConnect.Model.Ping.Success) {
                _dappEvents.tryEmit(pingSuccess)
            }

            override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                _dappEvents.tryEmit(pingError)
            }
        })
    }

    fun disconnect() {
        val disconnectParams = WalletConnect.Params.Disconnect(
            sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic),
            reason = "Disconnect Clicked",
            reasonCode = 400
        )

        WalletConnectClient.disconnect(disconnectParams, object : WalletConnect.Listeners.SessionDelete {
            override fun onSuccess(deletedSessionSuccess: WalletConnect.Model.DeletedSession.Success) {
                _dappEvents.tryEmit(deletedSessionSuccess)
            }

            override fun onError(deletedSessionError: WalletConnect.Model.DeletedSession.Error) {
                _dappEvents.tryEmit(deletedSessionError)
            }
        })
    }

    fun getListOfAccounts(topic: String? = null): List<SessionUI> {
        return WalletConnectClient.getListOfSettledSessions().filter {
            if (topic != null) {
                it.topic == topic
            } else {
                it.topic == DappDelegate.selectedSessionTopic
            }
        }.map { settledSession ->
            settledSession.accounts
        }.flatten().map {
            val (parentChain, chainId, account) = it.split(":")

            val chain = Chains.values().first { chain ->
                chain.parentChain == parentChain && chain.chainId == chainId.toInt()
            }

            SessionUI(chain.icon, chain.name, account, chain.parentChain, chain.chainId)
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

    fun requestMethod(method: String) {
        requireNotNull(DappDelegate.selectedSessionTopic)
        requireNotNull(DappDelegate.selectedAccountDetails)

        val (parentChain, chainId, account) = DappDelegate.selectedAccountDetails!!

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

        WalletConnectClient.request(requestParams, object : WalletConnect.Listeners.SessionRequest {
            override fun onSuccess(response: WalletConnect.Model.JsonRpcResponse) {
                _dappEvents.tryEmit(response)
            }

            override fun onError(requestError: WalletConnect.Model.JsonRpcResponse.Error) {
                _dappEvents.tryEmit(requestError)
            }
        })
    }

    fun updateSelectedChainUI(position: Int, isChecked: Boolean) {
        _listOfChainUI[position].isSelected = isChecked
    }

    fun deselectAccount() {
        DappDelegate.deselectAccountDetails()
    }

    companion object {
        private val _listOfChainUI: MutableList<ChainSelectionUI> = Chains.values().mapTo(mutableListOf()) {
            ChainSelectionUI(it.chainName, it.parentChain, it.chainId, it.icon, it.methods)
        }
    }
}