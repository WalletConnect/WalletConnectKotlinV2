package com.walletconnect.requester.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.requester.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<SessionDetailsUI> = MutableStateFlow(getSession())
    val uiState: StateFlow<SessionDetailsUI> = _uiState.asStateFlow()

    fun getSession(): SessionDetailsUI {
        return if (CacaoStore.currentCacao != null) {
            FetchingUI(CacaoStore.currentCacao!!.payload.address)
        } else {
            FetchingUI(INVALID_CACAO)
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            delay(2000L)
            _uiState.value = if (CacaoStore.currentCacao != null) {
                ConnectedUI(R.drawable.app_icon_round, CacaoStore.currentCacao!!.payload.address)
            } else {
                ConnectedUI(R.drawable.app_icon_round, INVALID_CACAO)
            }
        }
    }

    companion object {
        const val INVALID_CACAO = "Invalid Cacao state"
    }
}