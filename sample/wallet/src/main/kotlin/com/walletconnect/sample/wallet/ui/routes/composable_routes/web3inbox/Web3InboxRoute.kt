package com.walletconnect.sample.wallet.ui.routes.composable_routes.web3inbox

import androidx.compose.runtime.Composable
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.ui.Web3InboxState

@Composable
fun Web3InboxRoute(state: Web3InboxState) {
    Web3Inbox.View(state = state)
}