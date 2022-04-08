package com.walletconnect.dapp.domain

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object DappDelegate : WalletConnectClient.DappDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<WalletConnect.Model?> = _wcEventModels

    var selectedSessionTopic: String? = null
        private set
    var selectedAccountDetails: Triple<String, String, String>? = null
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

    override fun onSessionUpdateAccounts(updatedSession: WalletConnect.Model.UpdatedSessionAccounts) {
        scope.launch {
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionUpdateMethods(updatedSession: WalletConnect.Model.UpdatedSessionMethods) {

    }

    override fun onSessionUpdateEvents(updatedSession: WalletConnect.Model.UpdatedSessionEvents) {

    }

//    override fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession) {
//        scope.launch {
//            _wcEventModels.emit(upgradedSession)
//        }
//    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onUpdateSessionExpiry(session: WalletConnect.Model.Session) {
        //session extend
    }

    override fun onSessionPayloadResponse(response: WalletConnect.Model.SessionPayloadResponse) {
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    fun setSelectedAccountDetails(accountDetails: String) {
        val (parentChain, chainId, account) = accountDetails.split(":")
        selectedAccountDetails = Triple(parentChain, chainId, account)
    }

    fun deselectAccountDetails() {
        selectedAccountDetails = null
        selectedSessionTopic = null
    }
}