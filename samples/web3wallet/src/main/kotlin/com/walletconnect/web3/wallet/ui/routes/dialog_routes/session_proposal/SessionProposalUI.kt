package com.walletconnect.web3.wallet.ui.routes.dialog_routes.session_proposal

import com.walletconnect.web3.wallet.ui.common.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet

data class SessionProposalUI(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
)


private val extensiveSessionProposalUI = SessionProposalUI(
    peerUI = PeerUI(
        peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
        peerName = "Kotlin.Responder",
        peerUri = "kotlin.responder.app",
        peerDescription = ""
    ),
    namespaces = mapOf(
        "eip155" to Wallet.Model.Namespace.Proposal(
            chains = listOf("eip155:1", "eip155:137"),
            methods = listOf(
                "accountsChanged", "personalSign",
            ),
            events = listOf("someEvent1", "someEvent2"),
            extensions = listOf(
                Wallet.Model.Namespace.Proposal.Extension(
                    chains = listOf("eip155:1"),
                    methods = listOf("eth_signTypedData"),
                    events = listOf("someEvent3"),
                )
            )
        ),
        "cosmos" to Wallet.Model.Namespace.Proposal(
            chains = listOf("cosmos:cosmoshub-4", "cosmos:cosmoshub-1"),
            methods = listOf("accountsChanged", "personalSign"),
            events = listOf("someEvent1", "someEvent2"),
            extensions = null
        )
    )
)

private val minimalSessionProposalUI = SessionProposalUI(
    peerUI = PeerUI(
        peerIcon = "https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png",
        peerName = "Kotlin.Responder",
        peerUri = "kotlin.responder.app",
        peerDescription = ""
    ),
    namespaces = mapOf(
        "eip155" to Wallet.Model.Namespace.Proposal(
            chains = listOf("eip155:1"),
            methods = listOf("accountsChanged", "personalSign"),
            events = listOf("someEvent1", "someEvent2"),
            extensions = null
        ),
    )
)

val sessionProposalUI = extensiveSessionProposalUI

