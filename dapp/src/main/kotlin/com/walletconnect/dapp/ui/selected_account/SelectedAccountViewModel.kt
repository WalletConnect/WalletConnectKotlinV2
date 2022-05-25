package com.walletconnect.dapp.ui.selected_account

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.*
import com.walletconnect.walletconnectv2.client.AuthClient
import com.walletconnect.walletconnectv2.client.WalletConnect
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SelectedAccountViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SelectedAccountUI> = MutableStateFlow(SelectedAccountUI.Initial)
    val uiState: StateFlow<SelectedAccountUI> = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<SampleDappEvents> = MutableSharedFlow()
    val event: SharedFlow<SampleDappEvents> = _event.asSharedFlow()

    init {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { walletEvent ->
                when (walletEvent) {
                    is WalletConnect.Model.UpdatedSession -> {
                        //todo: fix session update
//                        (uiState.value as? SelectedAccountUI.Content)?.let { currentState ->
//                            val (updatedAccountAddress, updatedSelectedAccount) = walletEvent.namespaces.accounts.map { updatedAccount ->
//                                val (parentChain, chainId, accountAddress) = updatedAccount.split(":")
//                                Triple(parentChain, chainId, accountAddress)
//                            }.first { (parentChain, chainId, _) ->
//                                val (currentParentChain, currentChainId, _) = currentState.selectedAccount.split(":")
//
//                                parentChain == currentParentChain && chainId == currentChainId
//                            }.let { (parentChain, chainId, accountAddress) ->
//                                accountAddress to "$parentChain:$chainId:$accountAddress"
//                            }
//
//                            _uiState.value = currentState.copy(account = updatedAccountAddress, selectedAccount = updatedSelectedAccount)
//                        }
                    }
                    is WalletConnect.Model.SessionRequestResponse -> {
                        val request = when (walletEvent.result) {
                            is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> {
                                val successResult = (walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcResult)
                                SampleDappEvents.RequestSuccess(successResult.result)
                            }
                            is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> {
                                val errorResult = (walletEvent.result as WalletConnect.Model.JsonRpcResponse.JsonRpcError)
                                SampleDappEvents.RequestPeerError("Error Message: ${errorResult.message}\n Error Code: ${errorResult.code}")
                            }
                        }

                        _event.emit(request)
                    }
                    is WalletConnect.Model.DeletedSession -> {
                        _event.emit(SampleDappEvents.Disconnect)
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun requestMethod(method: String, sendSessionRequestDeepLink: (Uri) -> Unit) {
        (uiState.value as? SelectedAccountUI.Content)?.let { currentState ->
            val (parentChain, chainId, account) = currentState.selectedAccount.split(":")
            val params: String = when {
                method.equals("personal_sign", true) -> getPersonalSignBody(account)
                method.equals("eth_sign", true) -> getEthSignBody(account)
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

            AuthClient.request(requestParams) {
                viewModelScope.launch {
                    _event.emit(SampleDappEvents.RequestError(it.throwable.localizedMessage ?: "Error trying to send request"))
                }
            }

            val sessionRequestDeepLinkUri = "wc:/${requestParams.sessionTopic})}/request".toUri()
            sendSessionRequestDeepLink(sessionRequestDeepLinkUri)
        }
    }

    fun fetchAccountDetails(selectedAccountInfo: String) {
        val (chainNamespace, chainReference, account) = selectedAccountInfo.split(":")
        val chainDetails = EthTestChains.values().first {
            it.chainNamespace == chainNamespace && it.chainReference == chainReference.toInt()
        }
        val listOfMethods: List<String> = AuthClient.getListOfSettledSessions().filter { session ->
            session.topic == DappDelegate.selectedSessionTopic
        }.flatMap { session ->
            session.namespaces.values.flatMap { namespace -> namespace.methods }
        }

        viewModelScope.launch {
            _uiState.value = SelectedAccountUI.Content(
                icon = chainDetails.icon,
                chainName = chainDetails.chainName,
                account = account,
                listOfMethods = listOfMethods,
                selectedAccount = selectedAccountInfo
            )
        }
    }
}