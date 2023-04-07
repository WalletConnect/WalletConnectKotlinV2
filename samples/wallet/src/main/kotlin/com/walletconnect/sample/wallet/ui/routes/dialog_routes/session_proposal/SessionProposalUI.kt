package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet

data class SessionProposalUI(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
)

data class WalletMetaData(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Session>,
)

val walletMetaData = WalletMetaData(
    peerUI = PeerUI(
        peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
        peerName = "Kotlin.Responder",
        peerUri = "kotlin.responder.app",
        peerDescription = ""
    ),
    namespaces = mapOf(
        "eip155" to Wallet.Model.Namespace.Session(
            chains = listOf("eip155:1", "eip155:137", "eip155:56"),
            methods = listOf("accountsChanged", "personalSign"),
            events = listOf("someEvent1", "someEvent2"),
            accounts = listOf()
        ),
        "cosmos" to Wallet.Model.Namespace.Session(
            chains = listOf("cosmos:cosmoshub-4", "cosmos:cosmoshub-1"),
            methods = listOf("accountsChanged", "personalSign"),
            events = listOf("someEvent1", "someEvent2"),
            accounts = listOf()
        )
    )
)