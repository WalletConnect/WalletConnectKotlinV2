package com.walletconnect.requester.ui.session


sealed class SessionDetailsUI

data class ConnectedUI(val icon: Int, val address: String) : SessionDetailsUI()

data class FetchingUI(val address: String) : SessionDetailsUI()
