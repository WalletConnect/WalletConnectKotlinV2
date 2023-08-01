package com.walletconnect.sample.web3inbox.ui.routes.select_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.sample.web3inbox.domain.WCMDelegate
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleEvents
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SelectAccountViewModel() : ViewModel() {

    val walletEvents = WCMDelegate.wcEventModels.map { walletEvent: Modal.Model? ->
        when (walletEvent) {
            is Modal.Model.ApprovedSession -> W3ISampleEvents.SessionApproved(walletEvent.accounts.first())
            is Modal.Model.RejectedSession -> W3ISampleEvents.SessionRejected
            else -> W3ISampleEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun disconnectOldSessions() {
        WalletConnectModal.getListOfActiveSessions().forEach {
            WalletConnectModal.disconnect(Modal.Params.Disconnect(it.topic), onSuccess = {}, onError = { error -> Timber.e(error.throwable) })
        }
    }
}
