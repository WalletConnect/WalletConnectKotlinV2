package com.walletconnect.wallet.ui.sessions.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample_common.EthChains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAccounts2
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.wallet.ui.SampleWalletEvents
import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.client.SignClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionDetailsViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SessionDetailsUI?> = MutableStateFlow(null)
    val uiState: StateFlow<SessionDetailsUI?> = _uiState.asStateFlow()

    private val _sessionDetails: MutableSharedFlow<SampleWalletEvents> = MutableSharedFlow()
    val sessionDetails: SharedFlow<SampleWalletEvents> = _sessionDetails.asSharedFlow()

    private var selectedSessionTopic: String? = null

    companion object {
        private const val anotherEvent = "someEvent"
        private const val anotherEthMethod = "someMethod"
    }

    init {
        WalletDelegate.wcEventModels
            .onEach { wcModel: Sign.Model? ->
                when (wcModel) {
                    is Sign.Model.SessionUpdateResponse.Result -> {
                        // TODO: Update UI once state synchronization
                        SampleWalletEvents.NoAction
                    }
                    is Sign.Model.DeletedSession.Success -> {
                        selectedSessionTopic = null
                        _sessionDetails.emit(SampleWalletEvents.Disconnect)
                    }
                    else -> SampleWalletEvents.NoAction
                }
            }
            .launchIn(viewModelScope)
    }

    fun getSessionDetails(sessionTopic: String) {
        val state = SignClient.getListOfSettledSessions().find { it.topic == sessionTopic }?.let { selectedSession ->
            selectedSessionTopic = sessionTopic

            val listOfChainAccountInfo = filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession)
            val selectedSessionPeerData: Sign.Model.AppMetaData = requireNotNull(selectedSession.metaData)
            val uiState = SessionDetailsUI.Content(
                icon = selectedSessionPeerData.icons.first(),
                name = selectedSessionPeerData.name,
                url = selectedSessionPeerData.url,
                description = selectedSessionPeerData.description,
                listOfChainAccountInfo = listOfChainAccountInfo,
                methods = selectedSession.namespaces.values.flatMap { it.methods }.joinToString("\n"),
                events = selectedSession.namespaces.values.flatMap { it.events }.joinToString("\n")
            )

            uiState
        } ?: SessionDetailsUI.NoContent

        viewModelScope.launch {
            _uiState.emit(state)
        }
    }

    fun deleteSession() {
        selectedSessionTopic?.let {
            val disconnect = Sign.Params.Disconnect(
                sessionTopic = it,
                reason = "User disconnected Session",
                reasonCode = 1000
            )

            SignClient.disconnect(disconnect) { error ->
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
            val ping = Sign.Params.Ping(it)
            SignClient.ping(ping, object : Sign.Listeners.SessionPing {

                override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                    viewModelScope.launch() {
                        _sessionDetails.emit(SampleWalletEvents.PingSuccess(pingSuccess.topic, System.currentTimeMillis()))
                    }
                }

                override fun onError(pingError: Sign.Model.Ping.Error) {
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
            val extend = Sign.Params.Extend(it)
            SignClient.extend(extend) { error -> Log.d("Error", "Extend session error: $error") }
        }
    }

    //fixme: Needs whole view rework. Base view on JS Wallet
    fun emitEvent() {
        // Right now: Emits first alphabetical event
        // How it should be: User should be able to emit desired event
        selectedSessionTopic?.let { topic ->
            AuthClient.getListOfSettledSessions().find { it.topic == topic }?.let { selectedSession ->
                allApprovedEventsWithChains(selectedSession)
                    .filter { (_, chains) -> chains.isNotEmpty() }
                    .let { eventWithChains ->
                        eventWithChains.keys.minOrNull()?.let { event ->
                            eventWithChains[event]!!.first().let { chain ->
                                WalletConnect.Params.Emit(topic, WalletConnect.Model.SessionEvent(event, "dummyData"), chain).let { sessionEvent ->
                                    AuthClient.emit(sessionEvent) { error -> Log.d("Error", "Extend session error: $error") }
                                    return // So as not to log error below
                                }
                            }
                        }
                    }
            }
        }
        Log.e(tag(this@SessionDetailsViewModel), "Event was not emitted")
    }

    //fixme: Needs whole view rework. Base view on JS Wallet
    fun updateNamespaces() {
        // Right now: Expand first (right now there's only eip155) namespace with another account, event and method. Works only once
        // How it should be: User can toggle every account, method, event and then call this method with state to be updated
        selectedSessionTopic?.let { topic ->
            AuthClient.getListOfSettledSessions().find { it.topic == topic }?.let { selectedSession ->
                selectedSession.namespaces.let { namespaces ->
                    namespaces.keys.firstOrNull()?.let { key ->
                        namespaces[key]!!.let { namespace ->
                            val secondAccount = namespace.accounts.firstOrNull()?.let { account ->
                                val (chainNamespace, chainReference, _) = account.split(":")
                                mapOfAccounts2
                                    .filter { (ethChain, _) -> ethChain.chainNamespace == chainNamespace && ethChain.chainReference == chainReference.toInt() }
                                    .map { (ethChain, address) -> "${ethChain.chainNamespace}:${ethChain.chainReference}:${address}" }
                                    .firstOrNull()
                            }
                            val accounts: MutableList<String> = namespace.accounts.toMutableList()
                            if(!accounts.contains(secondAccount) && secondAccount != null) {
                                accounts.add(secondAccount)
                            }

                            val methods: MutableList<String> = namespace.methods.toMutableList()
                            if(!methods.contains(anotherEthMethod)) {
                                methods.add(anotherEthMethod)
                            }

                            val events: MutableList<String> = namespace.events.toMutableList()
                            if(!events.contains(anotherEvent)) {
                                events.add(anotherEvent)
                            }

                            val expandedNamespaces = mapOf( key to WalletConnect.Model.Namespace.Session(accounts, methods, events, null))
                            val update = WalletConnect.Params.UpdateNamespaces(sessionTopic = topic, namespaces = expandedNamespaces)
                            AuthClient.update(update) { error -> Log.d("Error", "Sending update error: $error") }
                            return
                        }
                    }
                }
            }
        }
        Log.e(tag(this@SessionDetailsViewModel), "Update was not called")
    }

    // TODO: Once state sync is complete, replace updating UI from VM with event from WalletDelegate - SessionUpdateResponse
//        viewModelScope.launch {
//            _uiState.emit(updatedUIState)
//        }


    private fun filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession: Sign.Model.Session): List<SessionDetailsUI.Content.ChainAccountInfo> =
        mapOfAllAccounts.values
            .flatMap { accountsMap: Map<EthChains, String> ->
                val accountsMapID = mapOfAllAccounts.entries.associate { it.value to it.key }.getValue(accountsMap)
                accountsMap.toList().map { (ethChain, accountAddress) -> Triple(ethChain, accountAddress, accountsMapID) }
            }
            .filter { (ethChain: EthChains, _, _) ->
                val listOfParentChainsWChainId = selectedSession.namespaces.values.flatMap { it.accounts }.map {
                    val (chainNamespace, chainReference, _) = it.split(":")
                    "$chainNamespace:$chainReference"
                }

                "${ethChain.chainNamespace}:${ethChain.chainReference}" in listOfParentChainsWChainId
            }
            .sortedBy { (ethChain: EthChains, _, _) -> ethChain.order }
            .groupBy { (ethChain: EthChains, _: String, _: Int) -> ethChain }.values
            .map { it: List<Triple<EthChains, String, Int>> ->
                val chainDetails: EthChains = it.first().first
                val chainName = chainDetails.chainName
                val chainIcon = chainDetails.icon
                val parentChain = chainDetails.chainNamespace
                val chainId = chainDetails.chainReference

                val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> =
                    it.map { (ethChain: EthChains, accountAddress: String, accountsMapId: Int) ->
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

    private fun allApprovedEventsWithChains(selectedSession: WalletConnect.Model.Session): Map<String, List<String>> =
        selectedSession.namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.accounts.map { getChainFromAccount(it) }
            }.toMutableList().apply {
                if (namespace.extensions != null) {
                    addAll(namespace.extensions!!.flatMap { extension ->
                        extension.events.map { event ->
                            event to namespace.accounts.map { getChainFromAccount(it) }
                        }
                    })
                }
            }
        }.toMap()

    @JvmSynthetic
    private fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, accountAddress: String) = elements

        return "$namespace:$reference"
    }
}