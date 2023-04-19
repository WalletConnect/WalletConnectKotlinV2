package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sample_common.Chains
import com.walletconnect.sample_common.tag
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ChainSelectionViewModel : ViewModel() {

    private val chains: List<ChainSelectionUi> =
        Chains.values().map { it.toChainUiState() }

    private val _uiState = MutableStateFlow(chains)
    val uiState = _uiState.asStateFlow()

    val isAnyChainSelected: Boolean
        get() = uiState.value.any { it.isSelected }

    val isAnySettledParingExist: Boolean
        get() = CoreClient.Pairing.getPairings().isNotEmpty()

    fun updateChainSelectState(position: Int, selected: Boolean) {
        _uiState.update {
            it.toMutableList().apply {
                this[position] = it[position].copy(isSelected = !selected)
            }
        }
    }

    fun connectToWallet(pairingTopicPosition: Int = -1, onProposedSequence: (String) -> Unit = {}) {
        val pairing: Core.Model.Pairing = if (pairingTopicPosition > -1) {
            CoreClient.Pairing.getPairings()[pairingTopicPosition]
        } else {
            CoreClient.Pairing.create() { error ->
                throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
            }!!
        }

        val namespaces: Map<String, Sign.Model.Namespace.Proposal> =
            uiState.value
                .filter { it.isSelected && it.chainId != Chains.POLYGON_MATIC.chainId && it.chainId != Chains.ETHEREUM_KOVAN.chainId }
                .groupBy { it.chainNamespace }
                .map { (key: String, selectedChains: List<ChainSelectionUi>) ->
                    key to Sign.Model.Namespace.Proposal(
                        chains = selectedChains.map { it.chainId }, //OR uncomment if chainId is an index
                        methods = selectedChains.flatMap { it.methods }.distinct(),
                        events = selectedChains.flatMap { it.events }.distinct()
                    )
                }.toMap()


        val tmp = uiState.value
            .filter { it.isSelected && it.chainId == Chains.ETHEREUM_KOVAN.chainId }
            .groupBy { it.chainId }
            .map { (key: String, selectedChains: List<ChainSelectionUi>) ->
                key to Sign.Model.Namespace.Proposal(
                    methods = selectedChains.flatMap { it.methods }.distinct(),
                    events = selectedChains.flatMap { it.events }.distinct()
                )
            }.toMap()

        val optionalNamespaces: Map<String, Sign.Model.Namespace.Proposal> =
            uiState.value
                .filter { it.isSelected && it.chainId == Chains.POLYGON_MATIC.chainId }
                .groupBy { it.chainId }
                .map { (key: String, selectedChains: List<ChainSelectionUi>) ->
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
                Timber.tag(tag(this)).e(error.throwable.stackTraceToString())
            }
        )

    }
}
