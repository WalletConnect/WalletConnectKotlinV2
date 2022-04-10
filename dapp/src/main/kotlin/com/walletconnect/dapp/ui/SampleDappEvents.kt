package com.walletconnect.dapp.ui

import com.walletconnect.dapp.ui.selected_account.SelectedAccountUI
import com.walletconnect.dapp.ui.session.SessionUI

sealed class SampleDappEvents {

    object SessionApproved : SampleDappEvents()

    object SessionRejected : SampleDappEvents()

    data class PingSuccess(val topic: String) : SampleDappEvents()

    object PingError : SampleDappEvents()

    object Disconnect : SampleDappEvents()

    class UpdatedListOfAccounts(val listOfAccounts: List<SessionUI>) : SampleDappEvents()

    data class RequestSuccess(val result: String) : SampleDappEvents()

    data class RequestPeerError(val errorMsg: String) : SampleDappEvents()

    data class RequestError(val exceptionMsg: String) : SampleDappEvents()

    data class UpgradedSelectedAccountUI(val selectedAccountUI: SelectedAccountUI) : SampleDappEvents()

    object NoAction : SampleDappEvents()
}