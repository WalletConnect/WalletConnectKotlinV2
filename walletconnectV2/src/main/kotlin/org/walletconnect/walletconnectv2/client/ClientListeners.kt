package org.walletconnect.walletconnectv2.client

import java.net.URI

sealed interface WalletConnectClientListeners {
    fun interface Pairing : WalletConnectClientListeners {
        fun onSessionProposal(proposal: SessionProposal)
    }
}

data class SessionProposal(
    val name: String,
    val description: String,
    val dappUrl: String,
    val icon: List<URI>,
    val chains: List<String>,
    var methods: List<String>,
    val topic: String,
    val proposerPublicKey: String,
    val ttl: Long,
    val accounts: List<String> = listOf()
)