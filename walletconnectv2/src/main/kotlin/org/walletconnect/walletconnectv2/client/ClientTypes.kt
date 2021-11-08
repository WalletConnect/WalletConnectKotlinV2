package org.walletconnect.walletconnectv2.client

import android.app.Application
import org.walletconnect.walletconnectv2.common.AppMetaData

sealed class ClientTypes {

    data class InitialParams(
        val application: Application,
        val useTls: Boolean = true,
        val hostName: String = WALLET_CONNECT_URL,
        val apiKey: String = "",
        val isController: Boolean = true,
        val metadata: AppMetaData = AppMetaData()
    ) : ClientTypes()

    data class PairParams(val uri: String) : ClientTypes()

    data class ApproveParams(val proposal: SessionProposal, val accounts: List<String>)

    data class RejectParams(val rejectionReason: String, val proposalTopic: String)

    data class DisconnectParams(val topic: String, val reason: String) : ClientTypes()

    companion object {
        private const val WALLET_CONNECT_URL = "relay.walletconnect.com"
    }
}
