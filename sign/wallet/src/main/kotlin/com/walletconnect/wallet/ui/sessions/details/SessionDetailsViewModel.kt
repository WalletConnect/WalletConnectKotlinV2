package com.walletconnect.wallet.ui.sessions.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import com.walletconnect.push.wallet.client.PushWalletProtocol
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
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
            .onEach { wcModel: Sign.Model? ->
                when (wcModel) {
                    is Sign.Model.SessionUpdateResponse.Result -> {
                        // TODO: Update UI once state synchronization
                        SampleWalletEvents.NoAction
                    }
                    is Sign.Model.DeletedSession -> {
                        selectedSessionTopic = null
                        _sessionDetails.emit(SampleWalletEvents.Disconnect)
                    }
                    else -> SampleWalletEvents.NoAction
                }
            }
            .launchIn(viewModelScope)
    }

    fun getSessionDetails(sessionTopic: String) {
        val state = SignClient.getActiveSessionByTopic(sessionTopic)?.let { selectedSession ->
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
            val disconnect = Sign.Params.Disconnect(sessionTopic = it)

            SignClient.disconnect(disconnect) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
            selectedSessionTopic = null

            val pushTopic = PushWalletClient.getActiveSubscriptions().entries.first().value.topic
            PushWalletClient.delete(Push.Wallet.Params.Delete(pushTopic)) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
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
                        _sessionDetails.emit(
                            SampleWalletEvents.PingSuccess(
                                pingSuccess.topic,
                                System.currentTimeMillis()
                            )
                        )
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

    //fixme: Needs whole view rework. Base view on JS Sign
    fun emitEvent() {
        // Right now: Emits first alphabetical event
        // How it should be: User should be able to emit desired event
        selectedSessionTopic?.let { topic ->
            SignClient.getActiveSessionByTopic(topic)?.let { selectedSession ->
                allApprovedEventsWithChains(selectedSession)
                    .filter { (_, chains) -> chains.isNotEmpty() }
                    .let { eventWithChains ->
                        eventWithChains.keys.minOrNull()?.let { event ->
                            eventWithChains[event]!!.first().let { chain ->
                                Sign.Params.Emit(
                                    topic,
                                    Sign.Model.SessionEvent(event, "dummyData"),
                                    chain
                                ).let { sessionEvent ->
                                    SignClient.emit(sessionEvent) { error ->
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
            SignClient.getActiveSessionByTopic(topic)?.let { selectedSession ->
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

                    val chains = namespace.chains?.toMutableList()

                    val expandedNamespaces =
                        mapOf(key to Sign.Model.Namespace.Session(chains,  accounts, methods, events))
                    val update =
                        Sign.Params.Update(sessionTopic = topic, namespaces = expandedNamespaces)
                    SignClient.update(update) { error ->
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


    private fun filterAndMapAllWalletAccountsToSelectedSessionAccounts(selectedSession: Sign.Model.Session): List<SessionDetailsUI.Content.ChainAccountInfo> =
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

    private fun allApprovedEventsWithChains(selectedSession: Sign.Model.Session): Map<String, List<String>> =
        selectedSession.namespaces.values.flatMap { namespace ->
            namespace.events.map { event ->
                event to namespace.accounts.map { getChainFromAccount(it) }
            }
        }.toMap()

    private fun getChainFromAccount(accountId: String): String {
        val elements = accountId.split(":")
        if (elements.isEmpty() || elements.size != 3) return accountId
        val (namespace: String, reference: String, _: String) = elements

        return "$namespace:$reference"
    }
}