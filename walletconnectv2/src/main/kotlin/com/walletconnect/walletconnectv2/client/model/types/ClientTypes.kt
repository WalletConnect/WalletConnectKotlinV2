package com.walletconnect.walletconnectv2.client.model.types

import android.app.Application
import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel

sealed class ClientTypes {

    data class InitialParams(
        val application: Application,
        val useTls: Boolean = true,
        val hostName: String = WALLET_CONNECT_URL,
        val projectId: String = "",
        val isController: Boolean = true,
        val metadata: WalletConnectClientModel.AppMetaData = WalletConnectClientModel.AppMetaData()
    ) : ClientTypes()

    data class PairParams(val uri: String) : ClientTypes()

    data class ApproveParams(val proposal: WalletConnectClientModel.SessionProposal, val accounts: List<String>) : ClientTypes()

    data class RejectParams(val rejectionReason: String, val proposalTopic: String) : ClientTypes()

    data class DisconnectParams(val sessionTopic: String, val reason: String) : ClientTypes()

    data class ResponseParams(val sessionTopic: String, val jsonRpcResponse: WalletConnectClientModel.JsonRpcResponse) : ClientTypes()

    data class UpdateParams(val sessionTopic: String, val sessionState: WalletConnectClientModel.SessionState) : ClientTypes()

    data class UpgradeParams(val topic: String, val permissions: WalletConnectClientModel.SessionPermissions) : ClientTypes()

    data class PingParams(val topic: String) : ClientTypes()

    data class NotificationParams(val topic: String, val notification: WalletConnectClientModel.Notification) : ClientTypes()

    companion object {
        private const val WALLET_CONNECT_URL = "relay.walletconnect.com"
    }
}
