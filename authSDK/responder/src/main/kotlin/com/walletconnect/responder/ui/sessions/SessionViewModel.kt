package com.walletconnect.responder.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.responder.domain.ResponderDelegate
import kotlinx.coroutines.flow.*

class SessionViewModel : ViewModel() {
    val activeSessionUI: StateFlow<List<SessionUI>> = ResponderDelegate.wcEvents
        .filterNotNull()
        .map {
            emptyList<SessionUI>()
            //todo: add fetching current authenticated sessions
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(),  emptyList())
}