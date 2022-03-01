package com.walletconnect.dapp.domain

import android.util.Log
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DappDelegate : WalletConnectClient.DappDelegate {
    private val TAG: String = DappDelegate::class.java.canonicalName!!
    private val _wcEventModels: MutableSharedFlow<Pair<Long, WalletConnect.Model?>> = MutableSharedFlow(extraBufferCapacity = 2)
    val wcEventModels: SharedFlow<Pair<Long, WalletConnect.Model?>> = _wcEventModels

    var selectedSessionTopic: String? = null
        private set
    var selectedAccountDetails: Triple<String, String, String>? = null
        private set

    init {
        WalletConnectClient.setDappDelegate(this)
    }

    override fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing) {
        Log.d(TAG, "pairing settled")
    }

    override fun onPairingUpdated(pairing: WalletConnect.Model.PairingUpdate) {
        Log.d(TAG, "pairing updated")
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        Log.d(TAG, "session approved")
        selectedSessionTopic = approvedSession.topic

        _wcEventModels.tryEmit(System.currentTimeMillis() to approvedSession)
        _wcEventModels.tryEmit(System.currentTimeMillis() to null)
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        Log.d(TAG, "session rejected")

        _wcEventModels.tryEmit(System.currentTimeMillis() to rejectedSession)
        _wcEventModels.tryEmit(System.currentTimeMillis() to null)
    }

    override fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession) {
        Log.d(TAG, "session updated")

        _wcEventModels.tryEmit(System.currentTimeMillis() to updatedSession)
    }

    override fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession) {
        Log.d(TAG, "session upgrade")

        _wcEventModels.tryEmit(System.currentTimeMillis() to upgradedSession)
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        Log.d(TAG, "session deleted")

        selectedAccountDetails = null
        selectedSessionTopic = null

        _wcEventModels.tryEmit(System.currentTimeMillis() to deletedSession)
    }

    override fun onSessionPayloadResponse(response: WalletConnect.Model.SessionPayloadResponse) {
        Log.d(TAG, "session payload response")

        _wcEventModels.tryEmit(System.currentTimeMillis() to response)
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