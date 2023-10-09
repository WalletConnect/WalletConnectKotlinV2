package com.walletconnect.web3.modal.ui.navigation

//Todo Think about split it into own graphs routes

enum class Route(val path: String, val title: String? = null) {
    //Common
    WEB3MODAL("web3_modal"),
    CHOOSE_NETWORK("choose_network", "Choose Network"),
    WHAT_IS_NETWORK("what_is_network", "What is a network?"),

    //Connect routes
    CONNECT_YOUR_WALLET("connect_your_wallet", "Connect Your Wallet"),
    QR_CODE("qr_code", "Mobile Wallets"),
    WHAT_IS_WALLET("what_is_wallet", "What is Wallet?"),
    GET_A_WALLET("get_a_wallet", "Get a Wallet"),
    ALL_WALLETS("all_wallets", "All wallets"),
    REDIRECT("redirect"),

    //Session routes
    ACCOUNT("account"),
    CHANGE_NETWORK("change_network", "Change Network"),
    CHAIN_SWITCH_REDIRECT("chain_switch_redirect"),
    RECENT_TRANSACTION("recent_transaction")
}
