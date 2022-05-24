package com.walletconnect.wallet.ui.sessions.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.wallet.ui.SampleWalletEvents
import com.walletconnect.walletconnectv2.client.AuthClient
import com.walletconnect.walletconnectv2.client.WalletConnect
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
                    is WalletConnect.Model.SessionUpdateResponse.Result -> {
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
        val state = AuthClient.getListOfSettledSessions().find { it.topic == sessionTopic }?.let { selectedSession ->
            selectedSessionTopic = sessionTopic

            val listOfChainAccountInfo = filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession)
            val selectedSessionPeerData: WalletConnect.Model.AppMetaData = requireNotNull(selectedSession.metaData)
            val uiState = SessionDetailsUI.Content(
                icon = selectedSessionPeerData.icons.first(),
                name = selectedSessionPeerData.name,
                url = selectedSessionPeerData.url,
                description = selectedSessionPeerData.description,
                listOfChainAccountInfo = listOfChainAccountInfo,
                methods = selectedSession.namespaces.values.flatMap { it.methods }.joinToString("\n")
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

            AuthClient.disconnect(disconnect) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
            selectedSessionTopic = null
        }

        viewModelScope.launch {
            _sessionDetails.emit(SampleWalletEvents.Disconnect)
        }
    }

    fun ping() {
        selectedSessionTopic?.let {
            val ping = WalletConnect.Params.Ping(it)
            AuthClient.ping(ping, object : WalletConnect.Listeners.SessionPing {

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

    fun extendSession() {
        selectedSessionTopic?.let {
            val extend = WalletConnect.Params.Extend(it)
            AuthClient.extend(extend) { error -> Log.d("Error", "Extend session error: $error") }
        }
    }

    fun emitEvent() {
        selectedSessionTopic?.let {
            //Hardcoded data for test purposes
            val extend = WalletConnect.Params.Emit(it, WalletConnect.Model.SessionEvent("testEvent", "testData"), "eip155:42")
            AuthClient.emit(extend) { error -> Log.d("Error", "Extend session error: $error") }
        }
    }

    fun updateNamespace() {
        //todo: commented and hardcoded new namespaces
//        (_uiState.value as? SessionDetailsUI.Content)?.let { sessionDetails ->
//            val listOfChainAccountInfo: List<SessionDetailsUI.Content.ChainAccountInfo> =
//                sessionDetails.listOfChainAccountInfo.map { chainAccountInfo ->
//                    if (chainAccountInfo.listOfAccounts.any { it.addressTitle == newUpdatedAccount.addressTitle }) {
//                        val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> =
//                            chainAccountInfo.listOfAccounts.map { account ->
//                                if (account.addressTitle == newUpdatedAccount.addressTitle) {
//                                    account.copy(isSelected = true)
//                                } else {
//                                    account.copy(isSelected = false)
//                                }
//                            }
//
//                        chainAccountInfo.copy(listOfAccounts = listOfAccounts)
//                    } else {
//                        chainAccountInfo
//                    }
//                }
//
//            val namespaces: Map<String, WalletConnect.Model.Namespace.Session> =
//                listOfChainAccountInfo.map { chainAccountInfo: SessionDetailsUI.Content.ChainAccountInfo ->
//                    chainAccountInfo.chainNamespace to chainAccountInfo.listOfAccounts.filter { it.isSelected }
//                        .map { account: SessionDetailsUI.Content.ChainAccountInfo.Account ->
//                            "${chainAccountInfo.chainNamespace}:${chainAccountInfo.chainReference}:${account.accountAddress}"
//                        }
//                }.groupBy { (chainNamespace, _) ->
//                    chainNamespace
//                }.map { (chainNamespace: String, accountDetails: List<Pair<String, List<String>>>) ->
//                    val accounts: List<String> = accountDetails.flatMap { it.second }
//                    val methods: List<String> = listOf("eth_sign")
//                    val events: List<String> = emptyList()
//
//                    chainNamespace to WalletConnect.Model.Namespace.Session(accounts = accounts,
//                        methods = methods,
//                        events = events,
//                        extensions = null)
//                }.toMap()

        selectedSessionTopic?.let { topic ->
            //hardcoded the namespaces structure
            val namespaces: Map<String, WalletConnect.Model.Namespace.Session> =
                mapOf(
                    "eip155" to WalletConnect.Model.Namespace.Session(
                        accounts =
                        listOf(
                            "eip155:4:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62",
                            "eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"
                        ),
                        methods = listOf("eth_sign", "eth_sendTransaction"),
                        events = listOf("new_testEvent"),
                        extensions = listOf(
                            WalletConnect.Model.Namespace.Session.Extension(
                                accounts = listOf("eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"),
                                methods = listOf("ethMain_sign"),
                                events = listOf("ethMain_event")
                            )
                        )
                    )
                )
            val update = WalletConnect.Params.Update(sessionTopic = topic, namespaces = namespaces)

            AuthClient.update(update) { error -> Log.d("Error", "Sending update error: $error") }
        }
    }

    // TODO: Once state sync is complete, replace updating UI from VM with event from WalletDelegate - SessionUpdateResponse
//        viewModelScope.launch {
//            _uiState.emit(updatedUIState)
//        }


    private fun filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession: WalletConnect.Model.Session): List<SessionDetailsUI.Content.ChainAccountInfo> =
        mapOfAllAccounts.values
            .flatMap { accountsMap: Map<EthTestChains, String> ->
                val accountsMapID = mapOfAllAccounts.entries.associate { it.value to it.key }.getValue(accountsMap)
                accountsMap.toList().map { (ethChain, accountAddress) -> Triple(ethChain, accountAddress, accountsMapID) }
            }
            .filter { (ethChain: EthTestChains, _, _) ->
                val listOfParentChainsWChainId = selectedSession.namespaces.values.flatMap { it.accounts }.map {
                    val (chainNamespace, chainReference, _) = it.split(":")
                    "$chainNamespace:$chainReference"
                }

                "${ethChain.chainNamespace}:${ethChain.chainReference}" in listOfParentChainsWChainId
            }
            .sortedBy { (ethChain: EthTestChains, _, _) -> ethChain.order }
            .groupBy { (ethChain: EthTestChains, _: String, _: Int) -> ethChain }.values
            .map { it: List<Triple<EthTestChains, String, Int>> ->
                val chainDetails: EthTestChains = it.first().first
                val chainName = chainDetails.chainName
                val chainIcon = chainDetails.icon
                val parentChain = chainDetails.chainNamespace
                val chainId = chainDetails.chainReference

                val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> =
                    it.map { (ethChain: EthTestChains, accountAddress: String, accountsMapId: Int) ->
                        val isSelected =
                            "${ethChain.chainNamespace}:${ethChain.chainReference}:$accountAddress" in selectedSession.namespaces.values.flatMap { it.accounts }
                        val addressTitle = "$accountAddress-Account $accountsMapId"

                        SessionDetailsUI.Content.ChainAccountInfo.Account(isSelected, addressTitle, accountAddress)
                    }

                SessionDetailsUI.Content.ChainAccountInfo(
                    chainName = chainName,
                    chainIcon = chainIcon,
                    chainNamespace = parentChain,
                    chainReference = chainId,
                    listOfAccounts = listOfAccounts
                )
            }
}