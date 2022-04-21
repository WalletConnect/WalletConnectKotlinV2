package com.walletconnect.wallet.ui.sessions.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.wallet.ui.SampleWalletEvents
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionDetailsViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SessionDetailsUI?> = MutableStateFlow(null)
    val uiState: StateFlow<SessionDetailsUI?> = _uiState.asStateFlow()

    private val _sessionDetails: MutableSharedFlow<SampleWalletEvents> = MutableSharedFlow()
    val sessionDetails: SharedFlow<SampleWalletEvents> = _sessionDetails.asSharedFlow()

    private var selectedSessionTopic: String? = null

    init {
        WalletDelegate.wcEventModels
            .onEach { wcModel: WalletConnect.Model? ->
                when (wcModel) {
                    is WalletConnect.Model.SessionUpdateAccountsResponse.Result -> {
                        // TODO: Update UI once state synchronization
                        SampleWalletEvents.NoAction
                    }
                    is WalletConnect.Model.DeletedSession.Success -> {
                        selectedSessionTopic = null
                        _sessionDetails.emit(SampleWalletEvents.Disconnect)
                    }
                    else -> SampleWalletEvents.NoAction
                }
            }
            .launchIn(viewModelScope)
    }

    fun getSessionDetails(sessionTopic: String) {
        val state = WalletConnectClient.getListOfSettledSessions().find { it.topic == sessionTopic }?.let { selectedSession ->
            selectedSessionTopic = sessionTopic

            val listOfChainAccountInfo = filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession)
            val selectedSessionPeerData: WalletConnect.Model.AppMetaData = requireNotNull(selectedSession.metaData)
            val uiState = SessionDetailsUI.Content(
                icon = selectedSessionPeerData.icons.first(),
                name = selectedSessionPeerData.name,
                url = selectedSessionPeerData.url,
                description = selectedSessionPeerData.description,
                listOfChainAccountInfo = listOfChainAccountInfo,
                methods = selectedSession.methods.joinToString("\n")
            )

            uiState
        } ?: SessionDetailsUI.NoContent

        viewModelScope.launch {
            _uiState.emit(state)
        }
    }

    fun deleteSession() {
        selectedSessionTopic?.let {
            val disconnect = WalletConnect.Params.Disconnect(
                sessionTopic = it,
                reason = "User disconnected Session",
                reasonCode = 1000
            )

            WalletConnectClient.disconnect(disconnect)
            selectedSessionTopic = null
        }

        viewModelScope.launch {
            _sessionDetails.emit(SampleWalletEvents.Disconnect)
        }
    }

    fun ping() {
        selectedSessionTopic?.let {
            val ping = WalletConnect.Params.Ping(it)
            WalletConnectClient.ping(ping, object : WalletConnect.Listeners.SessionPing {

                override fun onSuccess(pingSuccess: WalletConnect.Model.Ping.Success) {
                    viewModelScope.launch() {
                        _sessionDetails.emit(SampleWalletEvents.PingSuccess(pingSuccess.topic, System.currentTimeMillis()))
                    }
                }

                override fun onError(pingError: WalletConnect.Model.Ping.Error) {
                    viewModelScope.launch {
                        _sessionDetails.emit(SampleWalletEvents.PingError(System.currentTimeMillis()))
                    }
                }
            })
        } ?: viewModelScope.launch {
            _sessionDetails.emit(SampleWalletEvents.PingError(System.currentTimeMillis()))
        }
    }

    fun updateAccounts(newUpdatedAccount: SessionDetailsUI.Content.ChainAccountInfo.Account) {
        (_uiState.value as? SessionDetailsUI.Content)?.let { sessionDetails ->
            val listOfChainAccountInfo: List<SessionDetailsUI.Content.ChainAccountInfo> = sessionDetails.listOfChainAccountInfo.map { chainAccountInfo ->
                if (chainAccountInfo.listOfAccounts.any { it.addressTitle == newUpdatedAccount.addressTitle }) {
                    val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> = chainAccountInfo.listOfAccounts.map { account ->
                        if (account.addressTitle == newUpdatedAccount.addressTitle) {
                            account.copy(isSelected = true)
                        } else {
                            account.copy(isSelected = false)
                        }
                    }

                    chainAccountInfo.copy(listOfAccounts = listOfAccounts)
                } else {
                    chainAccountInfo
                }
            }

            val listOfSelectedAccounts = listOfChainAccountInfo.flatMap { chainAccountInfo ->
                chainAccountInfo.listOfAccounts.filter { it.isSelected }.map { account ->
                    "${chainAccountInfo.parentChain}:${chainAccountInfo.chainId}:${account.accountAddress}"
                }
            }
            selectedSessionTopic?.let {
                val update = WalletConnect.Params.UpdateAccounts(sessionTopic = it, accounts = listOfSelectedAccounts)

                WalletConnectClient.updateAccounts(update) { error -> Log.d("Error", "sending update error: $error") }
            }
        }

        // TODO: Once state sync is complete, replace updating UI from VM with event from WalletDelegate - SessionUpdateResponse
//        viewModelScope.launch {
//            _uiState.emit(updatedUIState)
//        }
    }

    fun updateMethods() {
        val updatedState = (_uiState.value as? SessionDetailsUI.Content)?.let { sessionDetails ->
            selectedSessionTopic?.let { sessionTopic ->
                val upgrade = WalletConnect.Params.UpdateMethods(sessionTopic = sessionTopic, methods = listOf("eth_sign"))
                WalletConnectClient.updateMethods(upgrade) { error -> Log.d("Error", "sending upgrade error: $error") }
            }

            sessionDetails.copy(methods = "eth_sign")
        }

        // TODO: Once state sync is complete, replace updating UI from VM with event from WalletDelegate - onSessionUpgradeResponse
        viewModelScope.launch {
            _uiState.emit(updatedState)
        }
    }

    private fun filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession: WalletConnect.Model.Session): List<SessionDetailsUI.Content.ChainAccountInfo> =
        mapOfAllAccounts.values
            .flatMap { accountsMap: Map<EthTestChains, String> ->
                val accountsMapID = mapOfAllAccounts.entries.associate { it.value to it.key }.getValue(accountsMap)
                accountsMap.toList().map { (ethChain, accountAddress) -> Triple(ethChain, accountAddress, accountsMapID) }
            }
            .filter { (ethChain: EthTestChains, _, _) ->
                val listOfParentChainsWChainId = selectedSession.accounts.map {
                    val (parentChain, chainId, account) = it.split(":")
                    "$parentChain:$chainId"
                }

                "${ethChain.parentChain}:${ethChain.chainId}" in listOfParentChainsWChainId
            }
            .sortedBy { (ethChain: EthTestChains, _, _) -> ethChain.order }
            .groupBy { (ethChain: EthTestChains, _: String, _: Int) -> ethChain }.values
            .map { it: List<Triple<EthTestChains, String, Int>> ->
                val chainDetails: EthTestChains = it.first().first
                val chainName = chainDetails.chainName
                val chainIcon = chainDetails.icon
                val parentChain = chainDetails.parentChain
                val chainId = chainDetails.chainId

                val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> = it.map { (ethChain: EthTestChains, accountAddress: String, accountsMapId: Int) ->
                    val isSelected = "${ethChain.parentChain}:${ethChain.chainId}:$accountAddress" in selectedSession.accounts
                    val addressTitle = "$accountAddress-Account $accountsMapId"

                    SessionDetailsUI.Content.ChainAccountInfo.Account(isSelected, addressTitle, accountAddress)
                }

                SessionDetailsUI.Content.ChainAccountInfo(
                    chainName = chainName,
                    chainIcon = chainIcon,
                    parentChain = parentChain,
                    chainId = chainId,
                    listOfAccounts = listOfAccounts
                )
            }
}