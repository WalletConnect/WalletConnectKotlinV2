package com.walletconnect.web3.inbox.webview

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.walletconnect.foundation.util.Logger
import com.walletconnect.util.generateId
import com.walletconnect.web3.inbox.common.model.AccountId
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC
import com.walletconnect.web3.inbox.proxy.ProxyInteractor
import com.walletconnect.web3.inbox.proxy.ProxyRequestHandler
import java.lang.ref.WeakReference

internal class WebViewInteractor(
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

    fun loadUrl() {
        logger.log("loadUrl")
        webViewWeakReference.webView.loadUrl(WEB3INBOX_URL)
    }

    internal companion object {
        const val WEB3INBOX_URL = "https://web3inbox-dev-hidden-git-fix-styling-and-3e38a9-walletconnect1.vercel.app?chatProvider=android"
        const val WEB3INBOX_JS_SIDE_PROXY_NAME = "android"
    }
}
