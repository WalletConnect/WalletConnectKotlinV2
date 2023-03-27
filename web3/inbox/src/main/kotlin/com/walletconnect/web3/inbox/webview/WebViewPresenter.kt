package com.walletconnect.web3.inbox.webview

import android.annotation.SuppressLint
import android.webkit.*
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
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            clearCache(true)
            addJavascriptInterface(proxyRequestHandler, WEB3INBOX_JS_SIDE_PROXY_NAME)
        })
    }


    internal fun web3InboxUrl(accountId: AccountId) = "$WEB3INBOX_URL${web3InboxUrlQueryParams(accountId)}"

    internal fun web3InboxUrlQueryParams(accountId: AccountId): WebViewQueryParams = WebViewQueryParams(
        mapOf(
            WEB3INBOX_PROVIDER_TYPE_KEY to WEB3INBOX_PROVIDER_TYPE_VALUE,
            WEB3INBOX_ACCOUNT_KEY to accountId.address()
        )
    )

    internal companion object {
        const val WEB3INBOX_URL = "https://web3inbox-dev-hidden.vercel.app"
        const val WEB3INBOX_PROVIDER_TYPE_KEY = "chatProvider"
        const val WEB3INBOX_PROVIDER_TYPE_VALUE = "android"
        const val WEB3INBOX_ACCOUNT_KEY = "account"
        const val WEB3INBOX_JS_SIDE_PROXY_NAME = "android"
    }
}
