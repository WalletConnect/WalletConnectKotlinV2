package com.walletconnect.wallet.ui.sessions.details

import androidx.annotation.DrawableRes

sealed class SessionDetailsUI {

    data class Content(
        val icon: String?,
        val name: String,
        val url: String,
        val description: String,
        val listOfChainAccountInfo: List<ChainAccountInfo>,
        val methods: String,
        val events: String,
    ): SessionDetailsUI() {

        data class ChainAccountInfo(
            val chainName: String,
            @DrawableRes val chainIcon: Int,
            val chainNamespace: String,
            val chainReference: String,
            val listOfAccounts: List<Account>
        ) {

            data class Account(val isSelected: Boolean, val addressTitle: String, val accountAddress: String)
        }
    }

    object NoContent: SessionDetailsUI()
}