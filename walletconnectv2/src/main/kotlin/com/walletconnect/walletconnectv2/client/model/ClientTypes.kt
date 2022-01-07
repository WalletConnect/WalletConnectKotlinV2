package com.walletconnect.walletconnectv2.client.model

import android.app.Application
import com.walletconnect.walletconnectv2.common.model.AppMetaData

sealed class ClientTypes {

    data class InitialParams(
        val application: Application,
        val useTls: Boolean = true,
        val hostName: String = WALLET_CONNECT_URL,
        val projectId: String = "",
        val isController: Boolean = true,
        val metadata: AppMetaData = AppMetaData()
    ) : ClientTypes()

    data class PairParams(val uri: String) : ClientTypes()

    data class ApproveParams(val proposal: WalletConnectClientData.SessionProposal, val accounts: List<String>) : ClientTypes()

    data class RejectParams(val rejectionReason: String, val proposalTopic: String) : ClientTypes()

    data class DisconnectParams(val sessionTopic: String, val reason: String) : ClientTypes()

    data class ResponseParams(val sessionTopic: String, val jsonRpcResponse: WalletConnectClientData.JsonRpcResponse) : ClientTypes()

    data class UpdateParams(val sessionTopic: String, val sessionState: WalletConnectClientData.SessionState) : ClientTypes()

    data class UpgradeParams(val topic: String, val permissions: WalletConnectClientData.SessionPermissions) : ClientTypes()

    data class PingParams(val topic: String) : ClientTypes()

    data class NotificationParams(val topic: String, val notification: WalletConnectClientData.Notification) : ClientTypes()

    companion object {
        private const val WALLET_CONNECT_URL = "relay.walletconnect.com"
    }
}
