package com.walletconnect.dapp.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.dapp.ui.connect.chain_select.ChainSelectionUI
import com.walletconnect.sample_common.EthChains
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.client.SignClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectViewModel : ViewModel() {
    private val _listOfChainUI: MutableList<ChainSelectionUI> = mutableListOf()
        get() {
            return if (field.isEmpty()) {
                EthChains.values().mapTo(field) {
                    ChainSelectionUI(it.chainName, it.chainNamespace, it.chainReference, it.icon, it.methods, it.events)
                }
            } else {
                field
            }
        }
    val listOfChainUI: List<ChainSelectionUI> = _listOfChainUI

    private val _navigation = Channel<SampleDappEvents>(Channel.BUFFERED)
    val navigation: Flow<SampleDappEvents> = _navigation.receiveAsFlow()

    init {
        DappDelegate.wcEventModels.map { walletEvent: Sign.Model? ->
            when (walletEvent) {
                is Sign.Model.ApprovedSession -> SampleDappEvents.SessionApproved
                is Sign.Model.RejectedSession -> SampleDappEvents.SessionRejected
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

    fun anySettledPairingExist(): Boolean = SignClient.getListOfSettledPairings().isNotEmpty()

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (Sign.Model.ProposedSequence) -> Unit = {}) {
        var pairingTopic: String? = null

        if (pairingTopicPosition > -1) {
            pairingTopic = SignClient.getListOfSettledPairings()[pairingTopicPosition].topic
        }

        val namespaces: Map<String, Sign.Model.Namespace.Proposal> =
            listOfChainUI
                .filter { it.isSelected }
                .groupBy { it.chainNamespace }
                .map { (key: String, selectedChains: List<ChainSelectionUI>) ->
                    key to Sign.Model.Namespace.Proposal(
                        chains = selectedChains.map { it.chainId },
                        methods = selectedChains.flatMap { it.methods }.distinct(),
                        events = selectedChains.flatMap { it.events }.distinct(),
                        extensions = null
                    )
                }.toMap()

        val connectParams = Sign.Params.Connect(
            namespaces = namespaces,
            pairingTopic = pairingTopic
        )

        SignClient.connect(connectParams,
            onProposedSequence = { proposedSequence ->
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(proposedSequence)
                }
            },
            onError = { error ->
                Log.e(tag(this@ConnectViewModel), error.throwable.stackTraceToString())
            }
        )
    }
}