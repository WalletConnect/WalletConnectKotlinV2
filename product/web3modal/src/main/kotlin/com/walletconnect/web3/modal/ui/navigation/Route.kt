package com.walletconnect.web3.modal.ui.navigation

//Todo Think about split it into own graphs routes

enum class Route(val path: String, val title: String? = null) {
    //Common
    WEB3MODAL("web3_modal"),
    CHOOSE_NETWORK("choose_network", "Select network"),
    //Connect routes
    CONNECT_YOUR_WALLET("connect_your_wallet", "Connect wallet"),
    QR_CODE("qr_code", "WalletConnect"),
    WHAT_IS_WALLET("what_is_wallet", "What is a wallet?"),
    GET_A_WALLET("get_a_wallet", "Get a wallet"),
    ALL_WALLETS("all_wallets", "All wallets"),
    REDIRECT("redirect"),

    //Session routes
    ACCOUNT("account"),
    CHANGE_NETWORK("change_network", "Select network"),
    CHAIN_SWITCH_REDIRECT("chain_switch_redirect"),
    RECENT_TRANSACTION("recent_transaction"),
    WHAT_IS_NETWORK("what_is_network", "What is a network?")
}
