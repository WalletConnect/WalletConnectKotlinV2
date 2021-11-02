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

    data class ApproveParams(val accounts: List<String>, val proposerPublicKey: String, val proposalTtl: Long, val proposalTopic: String)

    data class RejectParams(val rejectionReason: String, val proposalTopic: String)

    companion object {
        private const val WALLET_CONNECT_URL = "relay.walletconnect.com"
    }
}
