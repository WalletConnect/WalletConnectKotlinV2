package com.walletconnect.dapp.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.Chains
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.tag
import com.walletconnect.dapp.ui.NavigationEvents
import com.walletconnect.dapp.ui.connect.chain_select.ChainSelectionUI
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConnectViewModel : ViewModel() {
    private val _listOfChainUI: MutableList<ChainSelectionUI> = mutableListOf()
        get() {
            return if (field.isEmpty()) {
                Chains.values().mapTo(field) {
                    ChainSelectionUI(it.chainName, it.parentChain, it.chainId, it.icon, it.methods)
                }
            } else {
                field
            }
        }
    val listOfChainUI: List<ChainSelectionUI> = _listOfChainUI

    private val navigationChannel = Channel<NavigationEvents>(Channel.BUFFERED)
    val navigation = navigationChannel.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent: WalletConnect.Model? ->
            when (walletEvent) {
                is WalletConnect.Model.ApprovedSession -> NavigationEvents.SessionApproved
                is WalletConnect.Model.RejectedSession -> NavigationEvents.SessionRejected
                else -> NavigationEvents.NoAction
            }
        }.onEach { event ->
            navigationChannel.trySend(event)
        }.launchIn(viewModelScope)
    }

    fun updateSelectedChainUI(position: Int, isChecked: Boolean) {
        _listOfChainUI[position].isSelected = isChecked
    }

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (WalletConnect.Model.ProposedSequence) -> Unit = {}) {
        var pairingTopic: String? = null

        if (pairingTopicPosition > -1) {
            pairingTopic = WalletConnectClient.getListOfSettledPairings()[pairingTopicPosition].topic
        }

        val selectedChains: List<ChainSelectionUI> = listOfChainUI.filter { it.isSelected }
        val chains = selectedChains.map { "${it.parentChain}:${it.chainId}" }
        val methods = selectedChains.flatMap { it.methods }.distinct()
        val connectParams = WalletConnect.Params.Connect(
            chains = chains,
            methods = methods,
            events = listOf(),
            pairingTopic = pairingTopic)

        WalletConnectClient.connect(connectParams,
            onProposedSequence = { proposedSequence ->
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(proposedSequence)
                }

            },
            onFailure = { error -> Log.e(tag(this@ConnectViewModel), error.error.stackTraceToString()) })
    }
}