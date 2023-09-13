package com.walletconnect.web3.inbox.webview

import android.annotation.SuppressLint
import android.webkit.*
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.common.model.Config
import com.walletconnect.web3.inbox.common.proxy.ProxyRequestHandler
import java.lang.ref.WeakReference

internal class WebViewPresenter(
    private val proxyRequestHandler: ProxyRequestHandler,
    private val webViewWeakReference: WebViewWeakReference,
    private val logger: Logger,
    private val config: Config
) {
    private val _webViewClient: WebViewClient = WebViewClient()

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
            WEB3INBOX_CHAT_PROVIDER_TYPE_KEY to WEB3INBOX_PROVIDER_TYPE_VALUE,
            WEB3INBOX_AUTH_PROVIDER_TYPE_KEY to WEB3INBOX_AUTH_PROVIDER_TYPE_VALUE,
            WEB3INBOX_NOTIFY_PROVIDER_TYPE_KEY to WEB3INBOX_PROVIDER_TYPE_VALUE,
            WEB3INBOX_CHAT_ENABLED_KEY to config.isChatEnabled.toString(),
            WEB3INBOX_SETTINGS_ENABLED_KEY to config.areSettingsEnabled.toString(),
            WEB3INBOX_NOTIFY_ENABLED_KEY to config.isNotifyEnabled.toString(),
            WEB3INBOX_ACCOUNT_KEY to accountId.address()
        )
    )

    internal companion object {
//        const val WEB3INBOX_URL = "https://web3inbox-dev-hidden.vercel.app"
        const val WEB3INBOX_URL = "https://web3inbox-dev-hidden-git-chore-notif-refa-effa6b-walletconnect1.vercel.app/"

        const val WEB3INBOX_CHAT_PROVIDER_TYPE_KEY = "chatProvider"
        const val WEB3INBOX_NOTIFY_PROVIDER_TYPE_KEY = "notifyProvider"
        const val WEB3INBOX_AUTH_PROVIDER_TYPE_KEY = "authProvider"

        const val WEB3INBOX_CHAT_ENABLED_KEY = "chatEnabled"
        const val WEB3INBOX_SETTINGS_ENABLED_KEY = "settingsEnabled"
        const val WEB3INBOX_NOTIFY_ENABLED_KEY = "notifyEnabled"

        const val WEB3INBOX_PROVIDER_TYPE_VALUE = "android"
        const val WEB3INBOX_AUTH_PROVIDER_TYPE_VALUE = "android"

        const val WEB3INBOX_ACCOUNT_KEY = "account"
        const val WEB3INBOX_JS_SIDE_PROXY_NAME = "android"
    }
}
