package com.walletconnect.dapp.domain

import android.util.Log
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object DappDelegate : WalletConnectClient.DappDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<WalletConnect.Model?> =  _wcEventModels.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        WalletConnectClient.setDappDelegate(this)
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        selectedSessionTopic = approvedSession.topic

        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    override fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdateSession) {
        scope.launch {
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionExtend(session: WalletConnect.Model.Session) {
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: WalletConnect.Model.SessionRequestResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    fun deselectAccountDetails() {
        selectedSessionTopic = null
    }

    override fun onConnectionStateChange(state: WalletConnect.Model.ConnectionState) {
        Log.d(tag(this), "onConnectionStateChange($state)")
    }
}