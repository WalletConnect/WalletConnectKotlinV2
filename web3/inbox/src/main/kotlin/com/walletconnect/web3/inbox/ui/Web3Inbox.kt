package com.walletconnect.web3.inbox.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.web3.inbox.webview.WebViewPresenter

@Composable
internal fun Web3InboxView(
    modifier: Modifier = Modifier,
    webViewPresenter: WebViewPresenter,
    state: Web3InboxState
) {
    WebView(state = state.webViewState, modifier = modifier.fillMaxSize(), captureBackPresses = false, onCreated = { webView -> webViewPresenter.setWebView(webView) })
}

data class Web3InboxState(val webViewState: WebViewState)