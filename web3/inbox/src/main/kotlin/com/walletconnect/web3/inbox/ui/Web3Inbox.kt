package com.walletconnect.web3.inbox.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.web3.inbox.webview.WebViewPresenter

@Composable
internal fun Web3InboxView(
    modifier: Modifier = Modifier,
    webViewPresenter: WebViewPresenter,
    accountId: AccountId,
) {
    val state = rememberWebViewState(webViewPresenter.web3InboxUrl(accountId))

    WebView(state = state, modifier = modifier.fillMaxSize(), onCreated = { webView -> webViewPresenter.setWebView(webView) })
}