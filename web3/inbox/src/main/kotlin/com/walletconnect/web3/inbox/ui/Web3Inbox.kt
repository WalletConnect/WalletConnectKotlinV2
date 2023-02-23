package com.walletconnect.web3.inbox.ui

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.walletconnect.web3.inbox.webview.WebViewInteractor

@Composable
internal fun Web3InboxView(
    modifier: Modifier = Modifier,
    webViewInteractor: WebViewInteractor,
    state: WebViewState,
) {
    AndroidView(modifier = modifier, factory = { context -> createWebView(context, webViewInteractor, state) })
}

internal fun createWebView(context: Context, webViewInteractor: WebViewInteractor, state: WebViewState) = WebView(context).apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    webViewInteractor.setWebView(this)
    if (state == WebViewState.Loading) {
        webViewInteractor.loadUrl()
    }
}

internal sealed interface WebViewState {
    object Loading : WebViewState
    object Initialized : WebViewState
}