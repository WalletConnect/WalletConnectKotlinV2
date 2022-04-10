package com.walletconnect.dapp.ui.selected_account

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.*
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.getEthSendTransaction
import com.walletconnect.sample_common.getEthSignTypedData
import com.walletconnect.sample_common.getPersonalSignBody
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class SelectedAccountViewModel : ViewModel() {
    private val navigationChannel = Channel<SampleDappEvents>(Channel.BUFFERED)
    val navigation = navigationChannel.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent ->
            when {
                walletEvent is WalletConnect.Model.UpgradedSession -> {
                    val selectedAccountUI = getSelectedAccount()
                    SampleDappEvents.UpgradedSelectedAccountUI(selectedAccountUI)
                }
                walletEvent is WalletConnect.Model.SessionPayloadResponse && walletEvent.result is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> {
                    SampleDappEvents.RequestSuccess((walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcResult).result)
                }
                walletEvent is WalletConnect.Model.SessionPayloadResponse && walletEvent.result is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> {
                    val errorResult = (walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcError)
                    SampleDappEvents.RequestPeerError("Error Message: ${errorResult.message}\n Error Code: ${errorResult.code}")
                }
                walletEvent is WalletConnect.Model.DeletedSession -> SampleDappEvents.Disconnect
                else -> SampleDappEvents.NoAction
            }
        }.onEach { navigationEvents ->
            navigationChannel.trySend(navigationEvents)
        }.launchIn(viewModelScope)
    }

    fun requestMethod(method: String, sendSessionRequestDeepLink: (Uri) -> Unit) {
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
            navigationChannel.trySend(SampleDappEvents.RequestError(it.error.localizedMessage ?: "Error trying to send request"))
        }

        //TODO: Uncomment once refactor merged in
//        val sessionRequestDeepLinkUri = "wc:/${requireNotNull(DappDelegate.selectedSessionTopic)}".toUri()
//        sendSessionRequestDeepLink(sessionRequestDeepLinkUri)
    }

    fun getSelectedAccount(selectAccountDetails: String? = null): SelectedAccountUI {
        if (selectAccountDetails != null) {
            DappDelegate.setSelectedAccountDetails(selectAccountDetails)
        }

        val (parentChain, chainId, account) = requireNotNull(DappDelegate.selectedAccountDetails)
        val chainDetails = EthTestChains.values().first {
            it.parentChain == parentChain && it.chainId == chainId.toInt()
        }
        val listOfMethods: List<String> = WalletConnectClient.getListOfSettledSessions().filter {
            it.topic == DappDelegate.selectedSessionTopic
        }.flatMap {
            it.permissions.jsonRpc.methods
        }

        return SelectedAccountUI(chainDetails.icon, chainDetails.chainName, account, listOfMethods)
    }
}