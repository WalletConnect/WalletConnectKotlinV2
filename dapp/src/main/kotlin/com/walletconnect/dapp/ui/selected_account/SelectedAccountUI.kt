package com.walletconnect.dapp.ui.selected_account

data class SelectedAccountUI(
    val icon: Int,
    val chainName: String,
    val account: String,
    val listOfMethods: List<String>,
)
