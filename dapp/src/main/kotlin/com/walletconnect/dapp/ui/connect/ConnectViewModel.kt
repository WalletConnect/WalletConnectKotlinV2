package com.walletconnect.dapp.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.dapp.ui.connect.chain_select.ChainSelectionUI
import com.walletconnect.sample_common.EthTestChains
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectViewModel : ViewModel() {
    private val _listOfChainUI: MutableList<ChainSelectionUI> = mutableListOf()
        get() {
            return if (field.isEmpty()) {
                EthTestChains.values().mapTo(field) {
                    ChainSelectionUI(it.chainName, it.chainNamespace, it.chainReference, it.icon, it.methods)
                }
            } else {
                field
            }
        }
    val listOfChainUI: List<ChainSelectionUI> = _listOfChainUI

    private val _navigation = Channel<SampleDappEvents>(Channel.BUFFERED)
    val navigation: Flow<SampleDappEvents> = _navigation.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent: WalletConnect.Model? ->
            when (walletEvent) {
                is WalletConnect.Model.ApprovedSession -> SampleDappEvents.SessionApproved
                is WalletConnect.Model.RejectedSession -> SampleDappEvents.SessionRejected
                else -> SampleDappEvents.NoAction
            }
        }.onEach { event ->
            _navigation.trySend(event)
        }.launchIn(viewModelScope)
    }

    fun updateSelectedChainUI(position: Int, isChecked: Boolean) {
        _listOfChainUI[position].isSelected = isChecked
    }

    fun anyChainsSelected(): Boolean = listOfChainUI.any { it.isSelected }

    fun anySettledPairingExist(): Boolean = WalletConnectClient.getListOfSettledPairings().isNotEmpty()

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (WalletConnect.Model.ProposedSequence) -> Unit = {}) {
        var pairingTopic: String? = null

        if (pairingTopicPosition > -1) {
            pairingTopic = WalletConnectClient.getListOfSettledPairings()[pairingTopicPosition].topic
        }

        val namespaces: Map<String, WalletConnect.Model.Namespace.Proposal> =
            listOfChainUI.filter { it.isSelected }.groupBy { it.chainNamespace }.map { (key: String, selectedChains: List<ChainSelectionUI>) ->
                key to WalletConnect.Model.Namespace.Proposal(
                    chains = selectedChains.map { it.chainId },
                    methods = selectedChains.flatMap { it.methods }.distinct(),
                    events = listOf("testEvent"),
                    extensions = null
                )
            }.toMap()

        val connectParams = WalletConnect.Params.Connect(
            namespaces = namespaces,
            pairingTopic = pairingTopic)

        WalletConnectClient.connect(connectParams,
            onProposedSequence = { proposedSequence ->
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(proposedSequence)
                }
            },
            onError = { error -> Log.e(tag(this@ConnectViewModel), error.throwable.stackTraceToString()) })
    }
}