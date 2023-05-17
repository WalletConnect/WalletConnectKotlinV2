package com.walletconnect.sample.dapp.ui.routes.composable_routes.account

sealed class AccountUi {

    object Loading: AccountUi()

    data class AccountData(
        val icon: Int,
        val chainName: String,
        val account: String,
        val listOfMethods: List<String>,
        val selectedAccount: String
    ): AccountUi()
}

