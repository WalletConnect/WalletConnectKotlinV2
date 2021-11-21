package org.walletconnect.walletconnectv2.client

import java.net.URI

sealed class WalletConnectClientData {

    data class SessionProposal(
        val name: String,
        val description: String,
        val url: String,
        val icons: List<URI>,
        val chains: List<String>,
        var methods: List<String>,
        val topic: String,
        val proposerPublicKey: String,
        val ttl: Long
    ) : WalletConnectClientData() {
        val icon: String = icons.first().toString()
    }

    data class SessionRequest(
        val topic: String,
        val requestStringified: String,
        val chainId: String?,
        val method: String
    ) : WalletConnectClientData()

    data class SettledSession(
        var icon: String? = "",
        var name: String = "",
        var uri: String = "",
        val topic: String
    ) : WalletConnectClientData()

    data class SettledPairing(val topic: String) : WalletConnectClientData()
    data class RejectedSession(val topic: String) : WalletConnectClientData()
    data class DeletedSession(val topic: String) : WalletConnectClientData()
}