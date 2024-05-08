package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import com.walletconnect.sample.wallet.domain.ACCOUNTS_1_EIP155_ADDRESS
import com.walletconnect.sample.wallet.ui.common.peer.PeerContextUI
import com.walletconnect.sample.wallet.ui.common.peer.PeerUI
import com.walletconnect.web3.wallet.client.Wallet

data class SessionProposalUI(
    val peerUI: PeerUI,
    val namespaces: Map<String, Wallet.Model.Namespace.Proposal>,
    val optionalNamespaces: Map<String, Wallet.Model.Namespace.Proposal> = mapOf(),
    val peerContext: PeerContextUI,
    val redirect: String,
    val pubKey: String,
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
                "eth_signTypedData",
                "eth_signTypedData_v4"
            ),
            events = listOf("chainChanged", "accountsChanged"),
            accounts = listOf("eip155:1:$ACCOUNTS_1_EIP155_ADDRESS", "eip155:137:$ACCOUNTS_1_EIP155_ADDRESS", "eip155:56:$ACCOUNTS_1_EIP155_ADDRESS")
        ),
        "cosmos" to Wallet.Model.Namespace.Session(
            chains = listOf("cosmos:cosmoshub-4", "cosmos:cosmoshub-1"),
            methods = listOf("cosmos_signDirect", "cosmos_signAmino"),
            events = listOf(),
            accounts = listOf("cosmos:cosmoshub-4:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc", "cosmos:cosmoshub-1:cosmos1w605a5ejjlhp04eahjqxhjhmg8mj6nqhp8v6xc")
        ),
        "solana" to Wallet.Model.Namespace.Session(
            chains = listOf("solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp", "solana:8E9rvCKLFQia2Y35HXjjpWzj8weVo44K", "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp"),
            methods = listOf("solana_signMessage", "solana_signTransaction"),
            events = listOf(),
            accounts = listOf(
                "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp:FFiNdLBt9522PfCZQhWM28rHSPg9ekCa7rEZvBVf8NAf",
                "solana:8E9rvCKLFQia2Y35HXjjpWzj8weVo44K:FFiNdLBt9522PfCZQhWM28rHSPg9ekCa7rEZvBVf8NAf",
                "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp:FFiNdLBt9522PfCZQhWM28rHSPg9ekCa7rEZvBVf8NAf"
            )
        )
    )
)