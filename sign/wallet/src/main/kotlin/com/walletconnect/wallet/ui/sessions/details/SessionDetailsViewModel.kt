package com.walletconnect.wallet.ui.sessions.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.wallet.Wallet
import com.walletconnect.wallet.Wallet3Wallet
import com.walletconnect.wallet.domain.WalletDelegate
import com.walletconnect.wallet.domain.mapOfAccounts2
import com.walletconnect.wallet.domain.mapOfAllAccounts
import com.walletconnect.wallet.ui.SampleWalletEvents
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
            .onEach { wcModel: Wallet.Model? ->
                when (wcModel) {
                    is Wallet.Model.SessionUpdateResponse.Result -> {
                        // TODO: Update UI once state synchronization
                        SampleWalletEvents.NoAction
                    }
                    is Wallet.Model.SessionDelete -> {
                        selectedSessionTopic = null
                        _sessionDetails.emit(SampleWalletEvents.Disconnect)
                    }
                    else -> SampleWalletEvents.NoAction
                }
            }
            .launchIn(viewModelScope)
    }

    fun getSessionDetails(sessionTopic: String) {
        val state = Wallet3Wallet.getActiveSessionByTopic(sessionTopic)?.let { selectedSession ->
            selectedSessionTopic = sessionTopic

            val listOfChainAccountInfo =
                filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession)
            val selectedSessionPeerData: Core.Model.AppMetaData =
                requireNotNull(selectedSession.metaData)
            val uiState = SessionDetailsUI.Content(
                icon = selectedSessionPeerData.icons.firstOrNull(),
                name = selectedSessionPeerData.name,
                url = selectedSessionPeerData.url,
                description = selectedSessionPeerData.description,
                listOfChainAccountInfo = listOfChainAccountInfo,
                methods = selectedSession.namespaces.values.flatMap { it.methods }
                    .joinToString("\n"),
                events = selectedSession.namespaces.values.flatMap { it.events }
                    .joinToString("\n")
            )

            uiState
        } ?: SessionDetailsUI.NoContent

        viewModelScope.launch {
            _uiState.emit(state)
        }
    }

    fun deleteSession() {
        selectedSessionTopic?.let {
            val disconnect = Wallet.Params.SessionDisconnect(sessionTopic = it)

            Wallet3Wallet.disconnectSession(disconnect) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
            selectedSessionTopic = null
        }

        viewModelScope.launch {
            _sessionDetails.emit(SampleWalletEvents.Disconnect)
        }
    }

    fun ping() {
//        selectedSessionTopic?.let {
//            val ping = Wallet.Params.Ping(it)
//            WalletClient.ping(ping, object : Wallet.Listeners.SessionPing {
//
//                override fun onSuccess(pingSuccess: Wallet.Model.Ping.Success) {
//                    viewModelScope.launch() {
//                        _sessionDetails.emit(
//                            SampleWalletEvents.PingSuccess(
//                                pingSuccess.topic,
//                                System.currentTimeMillis()
//                            )
//                        )
//                    }
//                }
//
//                override fun onError(pingError: Wallet.Model.Ping.Error) {
//                    viewModelScope.launch {
//                        _sessionDetails.emit(SampleWalletEvents.PingError(System.currentTimeMillis()))
//                    }
//                }
//            })
//        } ?: viewModelScope.launch {
//            _sessionDetails.emit(SampleWalletEvents.PingError(System.currentTimeMillis()))
//        }
    }

    fun extendSession() {
        selectedSessionTopic?.let {
            val extend = Wallet.Params.SessionExtend(it)
            Wallet3Wallet.extendSession(extend) { error -> Log.d("Error", "Extend session error: $error") }
        }
    }

    //fixme: Needs whole view rework. Base view on JS Wallet
    fun emitEvent() {
        // Right now: Emits first alphabetical event
        // How it should be: User should be able to emit desired event
        selectedSessionTopic?.let { topic ->
            Wallet3Wallet.getActiveSessionByTopic(topic)?.let { selectedSession ->
                allApprovedEventsWithChains(selectedSession)
                    .filter { (_, chains) -> chains.isNotEmpty() }
                    .let { eventWithChains ->
                        eventWithChains.keys.minOrNull()?.let { event ->
                            eventWithChains[event]!!.first().let { chain ->
                                Wallet.Params.SessionEmit(
                                    topic,
                                    Wallet.Model.SessionEvent(event, "dummyData"),
                                    chain
                                ).let { sessionEvent ->
                                    Wallet3Wallet.emitSessionEvent(sessionEvent) { error ->
                                        Log.d(
                                            "Error",
                                            "Extend session error: $error"
                                        )
                                    }
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
            Wallet3Wallet.getActiveSessionByTopic(topic)?.let { selectedSession ->
                selectedSession.namespaces.firstNotNullOf { it }.let { (key, namespace) ->
                    val secondAccount = namespace.accounts.firstOrNull()?.let { account ->
                        val (chainNamespace, chainReference, _) = account.split(":")
                        mapOfAccounts2
                            .filter { (ethChain, _) -> ethChain.chainNamespace == chainNamespace && ethChain.chainReference == chainReference }
                            .map { (ethChain, address) -> "${ethChain.chainNamespace}:${ethChain.chainReference}:${address}" }
                            .firstOrNull()
                    }
                    val accounts: MutableList<String> = namespace.accounts.toMutableList()
                    if (!accounts.contains(secondAccount) && secondAccount != null) {
                        accounts.add(secondAccount)
                    }
                    val methods: MutableList<String> = namespace.methods.toMutableList()
                    if (!methods.contains(anotherEthMethod)) {
                        methods.add(anotherEthMethod)
                    }
                    val events: MutableList<String> = namespace.events.toMutableList()
                    if (!events.contains(anotherEvent)) {
                        events.add(anotherEvent)
                    }
                    val expandedNamespaces =
                        mapOf(key to Wallet.Model.Namespace.Session(accounts, methods, events, null))
                    val update =
                        Wallet.Params.SessionUpdate(sessionTopic = topic, namespaces = expandedNamespaces)
                    Wallet3Wallet.updateSession(update) { error ->
                        Log.e("Error", "Sending update error: $error")
                    }
                    return
                }
            }
        }
        Log.e(tag(this@SessionDetailsViewModel), "Update was not called")
    }

    // TODO: Once state sync is complete, replace updating UI from VM with event from WalletDelegate - SessionUpdateResponse
//        viewModelScope.launch {
//            _uiState.emit(updatedUIState)
//        }


    private fun filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession: Wallet.Model.Session): List<SessionDetailsUI.Content.ChainAccountInfo> =
        mapOfAllAccounts.values
            .flatMap { accountsMap: Map<Chains, String> ->
                val accountsMapID =
                    mapOfAllAccounts.entries.associate { it.value to it.key }.getValue(accountsMap)
                accountsMap.toList().map { (ethChain, accountAddress) ->
                    Triple(
                        ethChain,
                        accountAddress,
                        accountsMapID
                    )
                }
            }
            .filter { (ethChain: Chains, _, _) ->
                val listOfParentChainsWChainId =
                    selectedSession.namespaces.values.flatMap { it.accounts }.map {
                        val (chainNamespace, chainReference, _) = it.split(":")
                        "$chainNamespace:$chainReference"
                    }

                "${ethChain.chainNamespace}:${ethChain.chainReference}" in listOfParentChainsWChainId
            }
            .sortedBy { (ethChain: Chains, _, _) -> ethChain.order }
            .groupBy { (ethChain: Chains, _: String, _: Int) -> ethChain }.values
            .map { it: List<Triple<Chains, String, Int>> ->
                val chainDetails: Chains = it.first().first
                val chainName = chainDetails.chainName
                val chainIcon = chainDetails.icon
                val parentChain = chainDetails.chainNamespace
                val chainId = chainDetails.chainReference

                val listOfAccounts: List<SessionDetailsUI.Content.ChainAccountInfo.Account> =
                    it.map { (ethChain: Chains, accountAddress: String, accountsMapId: Int) ->
                        val isSelected =
                            "${ethChain.chainNamespace}:${ethChain.chainReference}:$accountAddress" in selectedSession.namespaces.values.flatMap { it.accounts }
                        val addressTitle = "$accountAddress-Account $accountsMapId"

                        SessionDetailsUI.Content.ChainAccountInfo.Account(
                            isSelected,
                            addressTitle,
                            accountAddress
                        )
                    }

                SessionDetailsUI.Content.ChainAccountInfo(
                    chainName = chainName,
                    chainIcon = chainIcon,
                    chainNamespace = parentChain,
                    chainReference = chainId,
                    listOfAccounts = listOfAccounts
                )
            }

    private fun allApprovedEventsWithChains(selectedSession: Wallet.Model.Session): Map<String, List<String>> =
        selectedSession.namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.accounts.map { getChainFromAccount(it) }
            }.plus(
                namespace.extensions?.flatMap { extension ->
                    extension.events.map { event ->
                        event to namespace.accounts.map { getChainFromAccount(it) }
                    }
                } ?: emptyList()
            )
        }.toMap()

    private fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, _: String) = elements

        return "$namespace:$reference"
    }
}