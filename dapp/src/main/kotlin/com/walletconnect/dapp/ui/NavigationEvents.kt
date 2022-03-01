package com.walletconnect.dapp.ui

import com.walletconnect.dapp.ui.selected_account.SelectedAccountUI
import com.walletconnect.dapp.ui.session.SessionUI

sealed class NavigationEvents {

    object SessionApproved : NavigationEvents()

    object SessionRejected: NavigationEvents()

    data class PingSuccess(val topic: String) : NavigationEvents()

    object PingError : NavigationEvents()

    object Disconnect : NavigationEvents()

    class UpdatedListOfAccounts(val listOfAccounts: List<SessionUI>) : NavigationEvents()

    data class RequestSuccess(val result: String): NavigationEvents()

    data class RequestPeerError(val errorMsg: String): NavigationEvents()

    data class RequestError(val exceptionMsg: String): NavigationEvents()

    data class UpgradedSelectedAccountUI(val selectedAccountUI: SelectedAccountUI) : NavigationEvents()

    object NoAction : NavigationEvents()
}