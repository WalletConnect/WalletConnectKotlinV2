package com.walletconnect.web3.inbox.webview

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.proxy.ProxyRequestHandler
import java.lang.ref.WeakReference

internal class WebViewPresenter(
    private val proxyRequestHandler: ProxyRequestHandler,
    private val webViewWeakReference: WebViewWeakReference,
    private val logger: Logger,
    private val onPageFinished: () -> Unit,
) {
    private val _webViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onPageFinished()
        }
    }

    private val _webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
            logger.log("${message.message()} -- From line ${message.lineNumber()} of ${message.sourceId()}")
            return true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setWebView(webView: WebView) {
        webViewWeakReference.reference = WeakReference(webView.apply {
            webChromeClient = _webChromeClient
            webViewClient = _webViewClient
            settings.javaScriptEnabled = true
            addJavascriptInterface(proxyRequestHandler, WEB3INBOX_JS_SIDE_PROXY_NAME)
        })
    }

    fun loadUrl(accountId: AccountId) {
        webViewWeakReference.webView.loadUrl(web3InboxUrl(accountId))
    }


    // todo: Add nice url builder.
    private fun web3InboxUrl(accountId: AccountId) = "$WEB3INBOX_URL$WEB3INBOX_PROVIDER_TYPE&account=${accountId.address()}"

    internal companion object {
        const val WEB3INBOX_URL = "https://web3inbox-dev-hidden.vercel.app"
        const val WEB3INBOX_PROVIDER_TYPE = "?chatProvider=android"
        const val WEB3INBOX_JS_SIDE_PROXY_NAME = "android"
    }
}
