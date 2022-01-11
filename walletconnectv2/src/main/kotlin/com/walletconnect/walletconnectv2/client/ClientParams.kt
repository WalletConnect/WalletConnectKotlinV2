package com.walletconnect.walletconnectv2.client

import android.app.Application

data class Init(
    val application: Application,
    val useTls: Boolean = true,
    val hostName: String = WALLET_CONNECT_URL,
    val projectId: String = "",
    val isController: Boolean = true,
    val metadata: AppMetaData = AppMetaData()
) : WalletConnectClient.Params()

data class Pair(val uri: String) : WalletConnectClient.Params()

data class Approve(val proposal: SessionProposal, val accounts: List<String>) : WalletConnectClient.Params()

data class Reject(val rejectionReason: String, val proposalTopic: String) : WalletConnectClient.Params()

data class Disconnect(val sessionTopic: String, val reason: String) : WalletConnectClient.Params()

data class Response(val sessionTopic: String, val jsonRpcResponse: JsonRpcResponse) : WalletConnectClient.Params()

data class Update(val sessionTopic: String, val sessionState: SessionState) : WalletConnectClient.Params()

data class Upgrade(val topic: String, val permissions: SessionPermissions) : WalletConnectClient.Params()

data class Ping(val topic: String) : WalletConnectClient.Params()

data class Notify(val topic: String, val notification: Notification) : WalletConnectClient.Params()
