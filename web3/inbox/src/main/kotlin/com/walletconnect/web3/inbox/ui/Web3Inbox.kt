package com.walletconnect.web3.inbox.ui

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.web3.inbox.webview.WebViewPresenter

@Composable
internal fun Web3InboxView(
    modifier: Modifier = Modifier,
    webViewPresenter: WebViewPresenter,
    state: WebViewState,
    accountId: AccountId,
) {
    AndroidView(modifier = modifier, factory = { context -> createWebView(context, webViewPresenter, state, accountId) })
}

internal fun createWebView(
    context: Context,
    webViewPresenter: WebViewPresenter,
    state: WebViewState,
    accountId: AccountId,
) = WebView(context).apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    webViewPresenter.setWebView(this)
    if (state == WebViewState.Loading) {
        webViewPresenter.loadUrl(accountId)
    }
}

internal sealed interface WebViewState {
    object Loading : WebViewState
    object Initialized : WebViewState
}