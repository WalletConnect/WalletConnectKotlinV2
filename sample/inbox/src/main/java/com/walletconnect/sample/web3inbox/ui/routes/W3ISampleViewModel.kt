package com.walletconnect.sample.web3inbox.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.web3inbox.domain.WCMDelegate
import com.walletconnect.wcmodal.client.Modal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class W3ISampleViewModel : ViewModel() {
    val wcmEvents = WCMDelegate
        .wcEventModels
        .map { event ->
            when (event) {
                is Modal.Model.DeletedSession -> W3ISampleEvents.Disconnect
                is Modal.Model.Session -> W3ISampleEvents.SessionExtend
                is Modal.Model.ConnectionState -> W3ISampleEvents.ConnectionEvent(event.isAvailable)
                is Modal.Model.Error -> W3ISampleEvents.RequestError(event.throwable.localizedMessage ?: "Something goes wrong")
                is Modal.Model.ApprovedSession -> W3ISampleEvents.SessionApproved(event.accounts.first())
                is Modal.Model.RejectedSession -> W3ISampleEvents.SessionRejected
                else -> W3ISampleEvents.NoAction
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

}

