package com.walletconnect.dapp.ui.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.utils.CURRENT_TIME_IN_SECONDS
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.dapp.ui.connect.chain_select.ChainSelectionUI
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class ConnectViewModel : ViewModel() {
    private val _listOfChainUI: MutableList<ChainSelectionUI> = mutableListOf()
        get() {
            return if (field.isEmpty()) {
                Chains.values().mapTo(field) {
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

    fun anySettledPairingExist(): Boolean = CoreClient.Pairing.getPairings().isNotEmpty()

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (String) -> Unit = {}) {
        val pairing: Core.Model.Pairing = if (pairingTopicPosition > -1) {
            CoreClient.Pairing.getPairings()[pairingTopicPosition]
        } else {
            CoreClient.Pairing.create() { error ->
                throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
            }!!
        }
        val namespaces: Map<String, Sign.Model.Namespace.Proposal> =
            listOfChainUI
                .filter { it.isSelected && it.chainId != Chains.POLYGON_MATIC.chainId && it.chainId != Chains.ETHEREUM_KOVAN.chainId }
                .groupBy { it.chainNamespace }
                .map { (key: String, selectedChains: List<ChainSelectionUI>) ->
                    key to Sign.Model.Namespace.Proposal(
                        chains = selectedChains.map { it.chainId }, //OR uncomment if chainId is an index
                        methods = selectedChains.flatMap { it.methods }.distinct(),
                        events = selectedChains.flatMap { it.events }.distinct()
                    )
                }.toMap()


        val tmp = listOfChainUI
            .filter { it.isSelected && it.chainId == Chains.ETHEREUM_KOVAN.chainId }
            .groupBy { it.chainId }
            .map { (key: String, selectedChains: List<ChainSelectionUI>) ->
                key to Sign.Model.Namespace.Proposal(
                    methods = selectedChains.flatMap { it.methods }.distinct(),
                    events = selectedChains.flatMap { it.events }.distinct()
                )
            }.toMap()

        val optionalNamespaces: Map<String, Sign.Model.Namespace.Proposal> =
            listOfChainUI
                .filter { it.isSelected && it.chainId == Chains.POLYGON_MATIC.chainId }
                .groupBy { it.chainId }
                .map { (key: String, selectedChains: List<ChainSelectionUI>) ->
                    key to Sign.Model.Namespace.Proposal(
                        methods = selectedChains.flatMap { it.methods }.distinct(),
                        events = selectedChains.flatMap { it.events }.distinct()
                    )
                }.toMap()

        //note: this property is not used in the SDK, only for demonstration purposes
        val expiry = (System.currentTimeMillis() / 1000) + TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
        val properties: Map<String, String> = mapOf("sessionExpiry" to "$expiry")

        val connectParams =
            Sign.Params.Connect(
                namespaces = namespaces.toMutableMap().plus(tmp),
                optionalNamespaces = optionalNamespaces,
                properties = properties,
                pairing = pairing
            )

        SignClient.connect(connectParams,
            onSuccess = {
                viewModelScope.launch(Dispatchers.Main) {
                    onProposedSequence(pairing.uri)
                }
            },
            onError = { error ->
                Log.e(tag(this@ConnectViewModel), error.throwable.stackTraceToString())
            }
        )
    }
}