package com.walletconnect.requester.ui.session

import androidx.lifecycle.ViewModel
import com.walletconnect.requester.R
import com.walletconnect.sample_common.Chains
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SessionDetailsUI> = MutableStateFlow(getSession())
    val uiState: StateFlow<SessionDetailsUI> = _uiState.asStateFlow()

    //todo: Reimplement. Right now only for demo purposes
    private fun getSession(): SessionDetailsUI =
        if (CacaoStore.currentCacao != null) {
            SessionDetailsUI(R.drawable.ic_ethereum, Chains.ETHEREUM_MAIN.chainName, CacaoStore.currentCacao!!.payload.address)
        } else {
            SessionDetailsUI(R.drawable.ic_ethereum, Chains.ETHEREUM_MAIN.chainName, "0x5496858C1f2f469Eb6A6D378C332e7a4E1dc1B4D")
        }
}