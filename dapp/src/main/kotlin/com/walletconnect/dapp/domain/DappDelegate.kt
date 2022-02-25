package com.walletconnect.dapp.domain

import android.util.Log
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DappDelegate : WalletConnectClient.DappDelegate {
    private val _wcEventModels: MutableSharedFlow<WalletConnect.Model> = MutableSharedFlow(extraBufferCapacity = 1)
    val wcEventModels: SharedFlow<WalletConnect.Model> = _wcEventModels

    var selectedPairingTopic: String? = null
        private set
    var selectedSessionTopic: String? = null
        private set
    var selectedAccountDetails: Triple<String, String, String>? = null
        private set

    init {
        WalletConnectClient.setDappDelegate(this)
    }

    override fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing) {
        Log.e(DappDelegate::class.java.canonicalName, "pairing settled")
    }

    override fun onPairingUpdated(pairing: WalletConnect.Model.SettledPairing) {
        Log.e(DappDelegate::class.java.canonicalName, "pairing updated")
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        Log.e(DappDelegate::class.java.canonicalName, "session approved")
        selectedSessionTopic = approvedSession.topic

        _wcEventModels.tryEmit(approvedSession)
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        TODO("Not yet implemented")
    }

    override fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession) {
        Log.e(DappDelegate::class.java.canonicalName, "session updated")

        _wcEventModels.tryEmit(updatedSession)
    }

    override fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession) {
        Log.e(DappDelegate::class.java.canonicalName, "session upgrade")

        _wcEventModels.tryEmit(upgradedSession)
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        Log.e(DappDelegate::class.java.canonicalName, "session deleted")

        _wcEventModels.tryEmit(deletedSession)
    }

    fun setSelectedPairingTopicOnConnect(topic: String) {
        selectedPairingTopic = topic
    }

    fun setSelectedAccountDetails(accountDetails: String) {
        val (parentChain, chainId, account) = accountDetails.split(":")
        selectedAccountDetails = Triple(parentChain, chainId, account)
    }

    fun deselectAccountDetails() {
        selectedAccountDetails = null
    }
}