package com.walletconnect.web3.inbox.webview

import android.webkit.WebView
import com.walletconnect.web3.inbox.common.exception.WebViewIsNullException
import java.lang.ref.WeakReference

internal class WebViewWeakReference {
    lateinit var reference: WeakReference<WebView>
    val webView get() = reference.get() ?: throw WebViewIsNullException()
}