package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import com.walletconnect.sample.wallet.domain.ACCOUNTS_1_EIP155_ADDRESS
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
        peerName = "Kotlin.Wallet",
        peerUri = "kotlin.wallet.app",
        peerDescription = ""
    ),
    namespaces = mapOf(
        "eip155" to Wallet.Model.Namespace.Session(
            chains = listOf("eip155:1", "eip155:137", "eip155:56"),
            methods = listOf(
                "eth_sendTransaction",
                "personal_sign",
                "eth_accounts",
                "eth_requestAccounts",
                "eth_call",
                "eth_getBalance",
                "eth_sendRawTransaction",
                "eth_sign",
                "eth_signTransaction",
                "eth_signTypedData"
            ),
            events = listOf("chainChanged", "accountsChanged"),
            accounts = listOf("eip155:1:$ACCOUNTS_1_EIP155_ADDRESS", "eip155:137:$ACCOUNTS_1_EIP155_ADDRESS", "eip155:56:$ACCOUNTS_1_EIP155_ADDRESS")
        ),
//        "cosmos" to Wallet.Model.Namespace.Session(
//            chains = listOf("cosmos:cosmoshub-4", "cosmos:cosmoshub-1"),
//            methods = listOf("accountsChanged", "personalSign"),
//            events = listOf("chainChanged", "chainChanged"),
//            accounts = listOf("cosmos:cosmoshub-4:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc", "cosmos:cosmoshub-1:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc")
//        )
    )
)