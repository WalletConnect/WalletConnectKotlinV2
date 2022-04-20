package com.walletconnect.dapp.ui.selected_account

sealed class SelectedAccountUI {

    object Initial: SelectedAccountUI()

    data class Content(
        val icon: Int,
        val chainName: String,
        val account: String,
        val listOfMethods: List<String>,
        val selectedAccount: String
    ): SelectedAccountUI()
}