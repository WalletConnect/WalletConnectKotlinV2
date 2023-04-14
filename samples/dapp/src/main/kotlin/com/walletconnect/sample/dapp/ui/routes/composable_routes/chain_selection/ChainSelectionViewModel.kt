package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import androidx.lifecycle.ViewModel
import com.walletconnect.android.CoreClient
import com.walletconnect.sample_common.Chains
import kotlinx.coroutines.flow.*

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
}
